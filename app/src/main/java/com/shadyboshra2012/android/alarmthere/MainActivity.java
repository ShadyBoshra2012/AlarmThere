package com.shadyboshra2012.android.alarmthere;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.shadyboshra2012.android.alarmthere.database.AlarmsDbHelper;
import com.shadyboshra2012.android.alarmthere.drawer_activities.InfoActivity;
import com.shadyboshra2012.android.alarmthere.drawer_activities.SettingsActivity;
import com.shadyboshra2012.android.alarmthere.drawer_activities.ShowAlarmsMapActivity;
import com.shadyboshra2012.android.alarmthere.newalarm.NewAlarmActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.add_new_alarm)
    FloatingActionButton addNewAlarm;
    @BindView(R.id.content_frame)
    FrameLayout contentFrame;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.no_alarm_layout)
    RelativeLayout noAlarmLayout;
    @BindView(R.id.adView)
    AdView adView;

    private static final int MY_PERMISSION_REQUEST_CODE = 11;
    private static final int PLAY_SERVICES_RESLUTION_REQUEST = 10;
    private static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;
    private static final int PICK_MAP_POINT_REQUEST = 999;  // The request code
    private static final int JOP_ID = 8898;
    private Intent AlarmServiceIntent;
    private JobInfo jobInfo;
    private JobScheduler jobScheduler;
    private BroadcastReceiver updateUIReciver;

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private ArrayList<Alarm> mAlarms = new ArrayList<Alarm>();
    private AlarmAdapter mAlarmsAdapter;
    private AlarmsDbHelper mDbHelper;

    private Snackbar locationSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 26) {
            ComponentName componentName = new ComponentName(this, AlarmJobService.class);
            jobInfo = new JobInfo.Builder(JOP_ID, componentName).setMinimumLatency(60000).build();
            jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        }
        else{
            AlarmServiceIntent = new Intent(this, AlarmService.class);
        }

        requestRuntimePermission();
        checkPermission();

        intializeLocationManger();

        setDrawerLayout();
        setToolbar();

        mDbHelper = new AlarmsDbHelper(this);

        startServiceAndLocation();

        mAlarmsAdapter = new AlarmAdapter(this, mAlarms);

        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                startServiceAndLocation();
                showLayout();
            }
        });

        listView.setAdapter(mAlarmsAdapter);

        startReceiver();

        showBannerAd();

        AppPreferences.getInstance(getApplicationContext()).incrementLaunchCount();
        showRateAppDialogIfNeeded();
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
        unregisterReceiver(updateUIReciver);
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    //Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        startServiceAndLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlarms.clear();
        mAlarms.addAll(mDbHelper.getAlarms(mDbHelper, null, null, null));
        mAlarmsAdapter.notifyDataSetChanged();

        showLayout();
        AppPreferences.getInstance(getApplicationContext()).showAd();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRateAppDialogIfNeeded() {
        boolean bool = AppPreferences.getInstance(getApplicationContext()).getAppRate();
        int i = AppPreferences.getInstance(getApplicationContext()).getLaunchCount();
        if ((bool) && (i % 5 == 0)) {
            createAppRatingDialog(getString(R.string.rate_app_title), getString(R.string.rate_app_message)).show();
        }
    }

    private AlertDialog createAppRatingDialog(String rateAppTitle, String rateAppMessage) {
        AlertDialog dialog = new AlertDialog.Builder(this).setPositiveButton(getString(R.string.dialog_app_rate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                openAppInPlayStore();
                AppPreferences.getInstance(MainActivity.this.getApplicationContext()).setAppRate(false);
            }
        }).setNegativeButton(getString(R.string.dialog_your_feedback), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                openFeedback(MainActivity.this);
                //AppPreferences.getInstance(MainActivity.this.getApplicationContext()).setAppRate(false);
                AppPreferences.getInstance(MainActivity.this.getApplicationContext()).resetLaunchCount();
            }
        }).setNeutralButton(getString(R.string.dialog_ask_later), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                paramAnonymousDialogInterface.dismiss();
                AppPreferences.getInstance(MainActivity.this.getApplicationContext()).resetLaunchCount();
            }
        }).setMessage(rateAppMessage).setTitle(rateAppTitle).create();
        return dialog;
    }

    public void openAppInPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarker = new Intent(Intent.ACTION_VIEW, uri);
        myAppLinkToMarker.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(myAppLinkToMarker);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    public static void openFeedback(Context paramContext) {
        Intent localIntent = new Intent(Intent.ACTION_SEND);
        localIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ShadyBoshra2011@gmail.com"});
        localIntent.putExtra(Intent.EXTRA_CC, "");
        String str = null;
        try {
            str = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
            localIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for AlarmThere");
            localIntent.putExtra(Intent.EXTRA_TEXT, "\n\n----------------------------------\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + str + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER);
            localIntent.setType("message/rfc822");
            paramContext.startActivity(Intent.createChooser(localIntent, "Choose an Email client :"));
        } catch (Exception e) {
           //Log.d("OpenFeedback", e.getMessage());
        }
    }

    private void showBannerAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.DISABLE_KEYGUARD
                }, MY_PERMISSION_REQUEST_CODE);
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setDrawerLayout() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.action_alarms:
                                //Do nothing
                                break;
                            case R.id.action_show_on_maps:
                                Intent showAlarmsMap = new Intent(getApplicationContext(), ShowAlarmsMapActivity.class);
                                startActivity(showAlarmsMap);
                                break;
                            case R.id.action_settings:
                                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(settings);
                                break;
                            case R.id.action_rate_app:
                                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                Intent myAppLinkToMarker = new Intent(Intent.ACTION_VIEW, uri);
                                myAppLinkToMarker.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(myAppLinkToMarker);
                                } catch (Exception e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                                }
                                break;
                            case R.id.action_send_feedback_app:
                                openFeedback(MainActivity.this);
                                break;
                            case R.id.action_info:
                                Intent info = new Intent(getApplicationContext(), InfoActivity.class);
                                startActivity(info);
                                break;
                        }

                        return true;
                    }
                });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    private void showLayout() {
        if (mAlarms.size() != 0) {
            listView.setVisibility(View.VISIBLE);
            noAlarmLayout.setVisibility(View.INVISIBLE);
        } else {
            noAlarmLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.add_new_alarm)
    public void onViewClicked() {
        AppPreferences.getInstance(getApplicationContext()).showAd();
        setNewAlarm();
    }

    private void setNewAlarm() {
        Alarm newAlarm = new Alarm();

        Intent pickPointIntent = new Intent(this, NewAlarmActivity.class);
        pickPointIntent.putExtra("newAlarm", newAlarm);
        startActivityForResult(pickPointIntent, PICK_MAP_POINT_REQUEST);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                checkPermission();
            } else {
                // Do as per your logic
            }

        }

        if (requestCode == PICK_MAP_POINT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Alarm newAlarm = (Alarm) data.getParcelableExtra("newAlarm");

                mDbHelper.insertAlarm(mDbHelper, newAlarm);
                mAlarms.add(newAlarm);

                mAlarmsAdapter.notifyDataSetChanged();

                startServiceAndLocation();
            }
        }
    }

    private boolean checkAlarmEnables() {
        for (int i = 0; i < mAlarms.size(); i++)
            if (mAlarms.get(i).isEnable())
                return true;

        if (!isGPSOn() && locationSnackBar != null)
            locationSnackBar.dismiss();

        return false;
    }

    private void startServiceAndLocation() {
        if (checkAlarmEnables()) {
            if (Build.VERSION.SDK_INT >= 26) {
                jobScheduler.schedule(jobInfo);
            } else {
                startService(AlarmServiceIntent);
            }

            Intent local = new Intent();
            local.setAction("com.shadyboshra2012.android.alarmthere.alarmservice2");
            sendBroadcast(local);

            intializeLocationManger();
        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                jobScheduler.cancel(JOP_ID);
            }else{
                stopService(AlarmServiceIntent);
            }

            if (mLocationManager != null) {
                for (int i = 0; i < mLocationListeners.length; i++) {
                    try {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                    } catch (Exception ex) {
                        //Log.i(TAG, "fail to remove location listners, ignore", ex);
                    }
                }
            }
        }
    }

    private void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.shadyboshra2012.android.alarmthere.alarmservice1");
        updateUIReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAlarms.clear();
                mAlarms.addAll(mDbHelper.getAlarms(mDbHelper, null, null, null));
                mAlarmsAdapter.notifyDataSetChanged();

                int alarmID = intent.getIntExtra("alarmID", -1);

                if (alarmID != -1)
                    for (int i = 0; i < mAlarms.size(); i++)
                        if (mAlarms.get(i).getID() == alarmID && !mAlarms.get(i).isSnoozed)
                            mAlarms.get(i).setEnable(false);
                startServiceAndLocation();
            }
        };
        registerReceiver(updateUIReciver, filter);
    }

    private void intializeLocationManger() {
        if (isGPSOn()) {
            if (locationSnackBar != null)
                locationSnackBar.dismiss();

            initializeLocationManager();
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (SecurityException ex) {
                //Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                //Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (SecurityException ex) {
                //Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                //Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }
        } else {
            if (!AppController.getInstance().isRejectTurnGPSOn()) {
                AppController.getInstance().setRejectTurnGPSOn(true);

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setPositiveButton(getResources().getString(R.string.location_dialog_turn_on), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                                Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(onGPS);
                            }
                        }).setNegativeButton(getResources().getString(R.string.location_dialog_cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                            }
                        }).setMessage(getResources().getString(R.string.location_dialog_message))
                        .setTitle(getResources().getString(R.string.location_dialog_title)).create();
                dialog.show();
            } else {
                locationSnackBar = Snackbar.make(mDrawerLayout, R.string.location_dialog_message, Snackbar.LENGTH_INDEFINITE);
                locationSnackBar.setAction(R.string.location_dialog_turn_on, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(onGPS);
                    }
                });
                locationSnackBar.show();
            }
        }
    }

    private boolean isGPSOn() {
        try {
            int isEnable = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            return (isEnable != 0);
        } catch (Exception e) {
            return true;
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);

            for (int i = 0; i < mAlarms.size(); i++) {
                Alarm alarm = mAlarms.get(i);
                alarm.userCurrentLatLng = mLastLocation;
            }

            mAlarmsAdapter.notifyDataSetChanged();
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

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}