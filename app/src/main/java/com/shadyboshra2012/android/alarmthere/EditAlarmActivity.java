package com.shadyboshra2012.android.alarmthere;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.shadyboshra2012.android.alarmthere.database.AlarmsDbHelper;
import com.shadyboshra2012.android.alarmthere.drawer_activities.InfoActivity;
import com.shadyboshra2012.android.alarmthere.drawer_activities.SettingsActivity;
import com.shadyboshra2012.android.alarmthere.drawer_activities.ShowAlarmsMapActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditAlarmActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.save_btn)
    Button saveBtn;
    @BindView(R.id.alarm_name_text)
    EditText alarmNameText;
    @BindView(R.id.location_name_text)
    EditText locationNameText;
    @BindView(R.id.range_distance_text)
    EditText rangeDistanceText;
    @BindView(R.id.unit_spinner)
    Spinner unitSpinner;
    @BindView(R.id.vicinity_name_text)
    EditText vicinityNameText;
    @BindView(R.id.colors_spinner)
    Spinner colorsSpinner;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;


    private GoogleMap mMap;
    private Alarm mAlarm;
    private AlarmsDbHelper mDbHelper;

    private Marker selectedPlaceMarker;
    private Circle selectedPlaceCircle;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        ButterKnife.bind(this);

        setDrawerLayout();
        setToolbar();

        mAlarm = getIntent().getParcelableExtra("alarm");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDbHelper = new AlarmsDbHelper(this);

        toolbar.setSubtitle(getResources().getString(R.string.subtitletitle_edit_activity, mAlarm.getName()));
        alarmNameText.setText(mAlarm.getName());
        locationNameText.setText(mAlarm.getPlaceName());
        rangeDistanceText.setText(mAlarm.getRangeDistance() + "");

        if (mAlarm.getVicinity() == null)
            vicinityNameText.setText("-");
        else
            vicinityNameText.setText(mAlarm.getVicinity());

        setMarkerColorSpinnerItem(mAlarm.getMarkerColor() + "");

        alarmNameText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {
                for (int i = s.length(); i > 0; i--) {
                    if (s.subSequence(i - 1, i).toString().equals("\n"))
                        s.replace(i - 1, i, "");
                }
            }
        });

        rangeDistanceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setChangeableCircleRange();
            }
        });

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setChangeableCircleRange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        colorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                markerColorChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
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
                                try{
                                    startActivity(myAppLinkToMarker);
                                }catch (Exception e){
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                                }
                                break;
                            case R.id.action_send_feedback_app:
                                openFeedback(EditAlarmActivity.this);
                                break;
                            case R.id.action_info:
                                Intent info = new Intent(getApplicationContext(), InfoActivity.class);
                                startActivity(info);
                                break;
                        }
                        finish();
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

    private void setChangeableCircleRange() {
        if (!rangeDistanceText.getText().toString().equals("")) {
            String meters_unit = getResources().getString(R.string.unit_meters);
            String kilo_meters_unit = getResources().getString(R.string.unit_kilo_meters);
            String mile_unit = getResources().getString(R.string.unit_mile);

            double range = Double.parseDouble(rangeDistanceText.getText().toString());
            String unit = unitSpinner.getSelectedItem().toString();

            if (unit.equals(meters_unit)) {
                range = range;
            } else if (unit.equals(kilo_meters_unit)) {
                range *= 1000;
            } else if (unit.equals(mile_unit)) {
                range = (range / 0.621371) * 1000;
            }

            if (range >= 100 && range <= 50000) {
                mAlarm.setRangeDistance(range);
                setSelectedPlaceCircle();
            }
        }
    }

    private void markerColorChanged(int position) {
        switch (position) {
            case 0:
                mAlarm.setMarkerColor(0.0f);
                break;
            case 1:
                mAlarm.setMarkerColor(210.0f);
                break;
            case 2:
                mAlarm.setMarkerColor(240.0f);
                break;
            case 3:
                mAlarm.setMarkerColor(180.0f);
                break;
            case 4:
                mAlarm.setMarkerColor(120.0f);
                break;
            case 5:
                mAlarm.setMarkerColor(300.0f);
                break;
            case 6:
                mAlarm.setMarkerColor(30.0f);
                break;
            case 7:
                mAlarm.setMarkerColor(330.0f);
                break;
            case 8:
                mAlarm.setMarkerColor(270.0f);
                break;
            case 9:
                mAlarm.setMarkerColor(60.0f);
                break;
        }

        setSelectedPlaceMarker();

        setSelectedPlaceCircle();
    }

    private void setMarkerColorSpinnerItem(String colorFloat) {
        switch (colorFloat) {
            case "0.0": //Red
                colorsSpinner.setSelection(0);
                break;
            case "210.0": //Azure
                colorsSpinner.setSelection(1);
                break;
            case "240.0": //Blue
                colorsSpinner.setSelection(2);
                break;
            case "180.0": //Cyan
                colorsSpinner.setSelection(3);
                break;
            case "120.0": //Green
                colorsSpinner.setSelection(4);
                break;
            case "300.0": //Magenta
                colorsSpinner.setSelection(5);
                break;
            case "30.0": //Orange
                colorsSpinner.setSelection(6);
                break;
            case "330.0": //Rose
                colorsSpinner.setSelection(7);
                break;
            case "270.0": //Violet
                colorsSpinner.setSelection(8);
                break;
            case "60.0": //Yellow
                colorsSpinner.setSelection(9);
                break;
            default:
                colorsSpinner.setSelection(0);
                break;
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

        setSelectedPlaceMarker();

        setSelectedPlaceCircle();
    }

    private void setSelectedPlaceMarker() {
        if (selectedPlaceMarker != null)
            selectedPlaceMarker.remove();

        selectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                .position(mAlarm.getLatLng())
                .title(mAlarm.getName() + "\n" + mAlarm.getVicinity())
                .icon(BitmapDescriptorFactory.defaultMarker(mAlarm.getMarkerColor())));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mAlarm.getLatLng(), 16f));
    }

    private void setSelectedPlaceCircle() {
        if (selectedPlaceCircle != null)
            selectedPlaceCircle.remove();

        selectedPlaceCircle = mMap.addCircle(new CircleOptions()
                .center(selectedPlaceMarker.getPosition())
                .radius(mAlarm.getRangeDistance())
                .fillColor((int) Long.parseLong(getColorString(mAlarm.getMarkerColor() + "", true), 16))
                .strokeColor((int) Long.parseLong(getColorString(mAlarm.getMarkerColor() + "", false), 16))
                .strokeWidth(2f));

        /*double kiloMeter = selectedPlaceRange / 1000;
        double mile = kiloMeter * 0.621371;
        rangeDistanceNumberText.setText(getString(R.string.range_distance_number, selectedPlaceRange + "", kiloMeter + "", mile + ""));*/
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

    @OnClick(R.id.save_btn)
    public void onViewClicked() {
        if (alarmNameText.getText().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.edit_activity_must_alarm_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (rangeDistanceText.getText().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.edit_activity_must_range_distance), Toast.LENGTH_SHORT).show();
            return;
        }

        String meters_unit = getResources().getString(R.string.unit_meters);
        String kilo_meters_unit = getResources().getString(R.string.unit_kilo_meters);
        String mile_unit = getResources().getString(R.string.unit_mile);

        double range = Double.parseDouble(rangeDistanceText.getText().toString());
        String unit = unitSpinner.getSelectedItem().toString();

        if (unit.equals(meters_unit))
            range = range;
        else if (unit.equals(kilo_meters_unit))
            range *= 1000;
        else if (unit.equals(mile_unit))
            range = (range / 0.621371) * 1000;

        if (range < 100 || range > 50000) {
            double defaultMinRange = 100;
            double defaultMaxRange = 50000;

            if (unit.equals(meters_unit)) {
                defaultMinRange = 100;
                defaultMaxRange = 50000;
            } else if (unit.equals(kilo_meters_unit)) {
                defaultMinRange /= 1000;
                defaultMaxRange /= 50000;
            } else if (unit.equals(mile_unit)) {
                defaultMinRange = (defaultMinRange / 1000) * 0.621371;
                defaultMaxRange = (defaultMaxRange / 1000) * 0.621371;
            }

            if (range < 100)
                Toast.makeText(this, getResources().getString(R.string.custom_range_distance_minimum_range_error, defaultMinRange + "", unit), Toast.LENGTH_SHORT).show();
            else if (range > 500000)
                Toast.makeText(this, getResources().getString(R.string.custom_range_distance_maximum_range_error, defaultMaxRange + "", unit), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.custom_range_please_set_range), Toast.LENGTH_SHORT).show();

            return;
        }

        mAlarm.setName(alarmNameText.getText().toString());
        mAlarm.setRangeDistance(range);

        mDbHelper.updateAlarm(mDbHelper, mAlarm);

        finish();
    }
}
