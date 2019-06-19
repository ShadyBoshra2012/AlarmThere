package com.shadyboshra2012.android.alarmthere;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.shadyboshra2012.android.alarmthere.database.AlarmsDbHelper;

import java.util.ArrayList;

public class AlarmJobIntentService extends JobIntentService {
    /* Give the Job a Unique Id */
    private static final int JOB_ID = 1000;

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private ArrayList<Alarm> mAlarms = new ArrayList<Alarm>();
    private AlarmsDbHelper mDbHelper;
    private BroadcastReceiver updateUIReciver;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        private LocationListener(String provider) {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            //Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            for (int i = 0; i < mAlarms.size(); i++) {
                Alarm alarm = mAlarms.get(i);

                if (alarm.isEnable() && !alarm.isRinging) {
                    Location selectedPlaceLocation = new Location("");
                    selectedPlaceLocation.setLatitude(alarm.getLatLng().latitude);
                    selectedPlaceLocation.setLongitude(alarm.getLatLng().longitude);


                    float distanceInMetersOne = selectedPlaceLocation.distanceTo(mLastLocation);

                    if (!alarm.isSnoozed) {
                        if (distanceInMetersOne <= alarm.getRangeDistance()) {

                            try {
                                showAlertDialog(alarm);
                                alarm.isRinging = true;
                                mDbHelper.updateAlarm(mDbHelper, alarm);
                            } catch (Exception e) {
                                //e.printStackTrace();
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        if (distanceInMetersOne <= alarm.snoozedRangeDistance) {

                            try {
                                showAlertDialog(alarm);
                                alarm.isRinging = true;
                                mDbHelper.updateAlarm(mDbHelper, alarm);
                            } catch (Exception e) {
                                //e.printStackTrace();
                                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        }

        //Dialog alert;

        private void showAlertDialog(final Alarm alarm) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();


            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
            kl.disableKeyguard();

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View alertView = inflater.inflate(R.layout.alert_view, null);
            TextView titleText = alertView.findViewById(R.id.title_text);
            Button dismissBtn = alertView.findViewById(R.id.dismiss_btn);
            Button snoozeBtn = alertView.findViewById(R.id.snooze_btn);
            final Dialog alert = new Dialog(getApplicationContext(), R.style.AlertDialogTheme);
            alert.setContentView(alertView);
            alert.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            titleText.setText(alarm.getName());
            snoozeBtn.setText(getResources().getString(R.string.alert_view_snoozed, (0.25 * alarm.snoozedRangeDistance) + "", getResources().getString(R.string.unit_meters)));

            dismissBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    r.stop();

                    mAlarms.remove(alarm);

                    alarm.isRinging = false;
                    alarm.setEnable(false);
                    alarm.isSnoozed = false;
                    alarm.snoozedRangeDistance = alarm.getRangeDistance();
                    mDbHelper.updateAlarm(mDbHelper, alarm);

                    mAlarms.add(alarm);

                    stopServiceAfterAlarmsDisable();

                    Intent local = new Intent();
                    local.setAction("com.shadyboshra2012.android.alarmthere.alarmservice1");
                    local.putExtra("alarmID", alarm.getID());
                    sendBroadcast(local);

                    alert.dismiss();
                }
            });

            snoozeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    r.stop();

                    mAlarms.remove(alarm);

                    alarm.isRinging = false;
                    alarm.isSnoozed = true;
                    alarm.snoozedRangeDistance -= 0.25 * alarm.snoozedRangeDistance;
                    mDbHelper.updateAlarm(mDbHelper, alarm);

                    mAlarms.add(alarm);

                    stopServiceAfterAlarmsDisable();

                    Intent local = new Intent();
                    local.setAction("com.shadyboshra2012.android.alarmthere.alarmservice");
                    local.putExtra("alarmID", alarm.getID());
                    sendBroadcast(local);

                    alert.dismiss();
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            else
                alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

            /*alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE |
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    AlarmJobIntentService.LocationListener[] mLocationListeners = new AlarmJobIntentService.LocationListener[]{
            new AlarmJobIntentService.LocationListener(LocationManager.GPS_PROVIDER),
            new AlarmJobIntentService.LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public static void enqueueWork(Context ctx, Intent intent) {
        enqueueWork(ctx, AlarmJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        /* your code here */
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mDbHelper = new AlarmsDbHelper(getApplicationContext());
                    mAlarms.addAll(mDbHelper.getAlarms(mDbHelper, null, null, null));
                    removeRingingFromAlarms();
                    startReceiver();

                    if (!checkAlarmEnables())
                        return;

                    initializeLocationManager();
                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                                mLocationListeners[1]);
                    } catch (java.lang.SecurityException ex) {
                        //Log.i(TAG, "fail to request location update, ignore", ex);
                    } catch (IllegalArgumentException ex) {
                        //Log.d(TAG, "network provider does not exist, " + ex.getMessage());
                    }
                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                                mLocationListeners[0]);
                    } catch (java.lang.SecurityException ex) {
                        //Log.i(TAG, "fail to request location update, ignore", ex);
                    } catch (IllegalArgumentException ex) {
                        //Log.d(TAG, "gps provider does not exist " + ex.getMessage());
                    }
                }
            });

        } catch (Exception e) {
        }
        /* reset the alarm */
        AlarmReceiver.setContext(getApplicationContext());
        AlarmReceiver.setAlarm(false);
        stopSelf();
    }


    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean checkAlarmEnables() {
        for (int i = 0; i < mAlarms.size(); i++)
            if (mAlarms.get(i).isEnable())
                return true;
        return false;
    }

    private void removeRingingFromAlarms() {
        for (int i = 0; i < mAlarms.size(); i++)
            if (mAlarms.get(i).isRinging) {
                mAlarms.get(i).isRinging = false;
                mDbHelper.updateAlarm(mDbHelper, mAlarms.get(i));
            }
    }

    private void stopServiceAfterAlarmsDisable() {
        if (!checkAlarmEnables())
            this.stopSelf();
    }

    private void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.shadyboshra2012.android.alarmthere.alarmservice2");
        updateUIReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAlarms.clear();
                mAlarms.addAll(mDbHelper.getAlarms(mDbHelper, null, null, null));
            }
        };
        unregisterReceiver(updateUIReciver);
        registerReceiver(updateUIReciver, filter);
    }
}
