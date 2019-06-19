package com.shadyboshra2012.android.alarmthere.drawer_activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.shadyboshra2012.android.alarmthere.Alarm;
import com.shadyboshra2012.android.alarmthere.AppPreferences;
import com.shadyboshra2012.android.alarmthere.MainActivity;
import com.shadyboshra2012.android.alarmthere.R;
import com.shadyboshra2012.android.alarmthere.database.AlarmsContract;
import com.shadyboshra2012.android.alarmthere.database.AlarmsDbHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowAlarmsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private GoogleMap mMap;
    private ArrayList<Alarm> mAlarms = new ArrayList<Alarm>();
    private AlarmsDbHelper mDbHelper;

    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    LatLngBounds bounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_alarms_map);
        ButterKnife.bind(this);

        mDbHelper = new AlarmsDbHelper(this);
        mAlarms.addAll(mDbHelper.getAlarms(mDbHelper, null, null, null));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setDrawerLayout();
        setToolbar();
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
                                finish();
                                break;
                            case R.id.action_show_on_maps:
                                //Do nothing
                                break;
                            case R.id.action_settings:
                                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(settings);
                                finish();
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
                                openFeedback(ShowAlarmsMapActivity.this);
                                break;
                            case R.id.action_info:
                                Intent info = new Intent(getApplicationContext(), InfoActivity.class);
                                startActivity(info);
                                finish();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for (Alarm alarm : mAlarms) {
            mMap.addMarker(new MarkerOptions()
                    .position(alarm.getLatLng())
                    .title(alarm.getName() + "\n" + alarm.getVicinity())
                    .icon(BitmapDescriptorFactory.defaultMarker(alarm.getMarkerColor())));

            mMap.addCircle(new CircleOptions()
                    .center(alarm.getLatLng())
                    .radius(alarm.getRangeDistance())
                    .fillColor((int) Long.parseLong(getColorString(alarm.getMarkerColor() + "", true), 16))
                    .strokeColor((int) Long.parseLong(getColorString(alarm.getMarkerColor() + "", false), 16))
                    .strokeWidth(2f));

            builder.include(alarm.getLatLng());
        }

        try {
            bounds = builder.build();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                }
            }, 2000);

            AppPreferences.getInstance(getApplicationContext()).showAd();
        } catch (Exception e) {
        }
    }

    private String getColorString(String colorFloat, boolean isAlpha) {
        switch (colorFloat) {
            case "0.0": //Red
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.RedMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.RedMarker).substring(2);
            case "210.0": //Azure
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.AzureMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.AzureMarker).substring(2);
            case "240.0": //Blue
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.BlueMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.BlueMarker).substring(2);
            case "180.0": //Cyan
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.CyanMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.CyanMarker).substring(2);
            case "120.0": //Green
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.GreenMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.GreenMarker).substring(2);
            case "300.0": //Magenta
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.MagentaMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.MagentaMarker).substring(2);
            case "30.0": //Orange
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.OrangeMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.OrangeMarker).substring(2);
            case "330.0": //Rose
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.RoseMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.RoseMarker).substring(2);
            case "270.0": //Violet
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.VioletMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.VioletMarker).substring(2);
            case "60.0": //Yellow
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.YellowMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.YellowMarker).substring(2);
            default:
                if (isAlpha)
                    return "AA" + getResources().getString(0 + R.color.RedMarker).substring(2);
                else
                    return "FF" + getResources().getString(0 + R.color.RedMarker).substring(2);
        }
    }
}
