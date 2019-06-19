package com.shadyboshra2012.android.alarmthere.newalarm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.shadyboshra2012.android.alarmthere.Alarm;
import com.shadyboshra2012.android.alarmthere.AppController;
import com.shadyboshra2012.android.alarmthere.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.GEOMETRY;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.GOOGLE_BROWSER_API_KEY;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.ICON;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.ID;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.LATITUDE;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.LOCALE_LANGUAGE;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.LOCATION;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.LONGITUDE;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.NAME;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.OK;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.PLACE_ID;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.PROXIMITY_RADIUS;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.REFERENCE;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.STATUS;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.TAG;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.VICINITY;
import static com.shadyboshra2012.android.alarmthere.newalarm.PlaceConfig.ZERO_RESULTS;

public class NewAlarmActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    @BindView(R.id.pick_place_text)
    TextView pickPlaceText;
    @BindView(R.id.nav_next_btn)
    FloatingActionButton navNextBtn;
    @BindView(R.id.pick_place_layout)
    LinearLayout pickPlaceLayout;
    @BindView(R.id.frame_layout)
    FrameLayout frameLayout;
    @BindView(R.id.range_distance_number_text)
    TextView rangeDistanceNumberText;
    @BindView(R.id.increament_btn)
    FloatingActionButton increamentBtn;
    @BindView(R.id.decreament_btn)
    FloatingActionButton decreamentBtn;
    @BindView(R.id.edit_btn)
    FloatingActionButton editBtn;
    @BindView(R.id.range_distance_layout)
    LinearLayout rangeDistanceLayout;
    @BindView(R.id.nav_set_alarm_btn)
    FloatingActionButton navSetAlarmBtn;
    @BindView(R.id.possible_location_list_view)
    ListView possibleLocationListView;
    @BindView(R.id.locations_layout)
    LinearLayout locationsLayout;
    @BindView(R.id.create_own_location_layout)
    LinearLayout createOwnLocationLayout;
    @BindView(R.id.error_message)
    TextView errorMessage;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private GoogleMap mMap;
    private static final float[] markerColors = {0.0f, 210.0f, 240.0f, 180.0f, 120.0f, 300.0f, 30.0f, 330.0f, 270.0f, 60.0f};
    private float randomMarkerColor;

    private static final int MY_PERMISSION_REQUEST_CODE = 11;
    private static final int PLAY_SERVICES_RESLUTION_REQUEST = 10;
    private Location mLastLocation;

    double latitude, longitude;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACMENT = 10;

    private Marker myCurrentLocation;
    private Marker selectedPlaceMarker;
    private Circle selectedPlaceCircle;
    private double selectedPlaceRange = 500;

    private Alarm newAlarm = new Alarm();

    private ArrayList<PossibleLocation> mPossibleLocations = new ArrayList<PossibleLocation>();
    private PossibleLocationAdapter mPossibleLocationAdapter;

    private Snackbar locationSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alarm);
        ButterKnife.bind(this);
        navNextBtn.setEnabled(false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        newAlarm = getIntent().getParcelableExtra("newAlarm");

        setUpLocation();

        mPossibleLocationAdapter = new PossibleLocationAdapter(this, mPossibleLocations);
        possibleLocationListView.setAdapter(mPossibleLocationAdapter);

        possibleLocationListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                for (int i = 0; i < mPossibleLocations.size(); i++) {
                    if (mPossibleLocations.get(i).isSelected) {
                        createOwnLocationLayout.setBackgroundResource(R.drawable.background_shape_list_item_disable);

                        if (selectedPlaceMarker != null)
                            selectedPlaceMarker.remove();

                        selectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                                .position(mPossibleLocations.get(i).getLatLng())
                                .title(mPossibleLocations.get(i).getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(randomMarkerColor)));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPossibleLocations.get(i).getLatLng(), 16f));

                        scrollMap(-200, 0, 1000);

                        navNextBtn.setEnabled(true);
                        navNextBtn.setClickable(true);
                        navNextBtn.setFocusable(true);
                        navNextBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

                        return;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (locationsLayout.getVisibility() == View.VISIBLE) {
            mPossibleLocations.clear();
            locationsLayout.setVisibility(View.GONE);

            navNextBtn.setEnabled(false);
            navNextBtn.setClickable(false);
            navNextBtn.setFocusable(false);
            navNextBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentDisable)));
        } else if (selectedPlaceCircle != null) {
            selectedPlaceCircle.remove();
            selectedPlaceCircle = null;

            rangeDistanceLayout.setVisibility(View.GONE);
            pickPlaceLayout.setVisibility(View.VISIBLE);
            locationsLayout.setVisibility(View.VISIBLE);

            setMapClickLisnter();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_REQUEST_CODE);
    }

    private void startLoctionUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "This device can't support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACMENT);
    }

    private boolean isGPSOn() {
        try {
            int isEnable = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            return (isEnable != 0);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isUserMoveMap = false;

    private void setCurrentLocationMarker() {
        if (myCurrentLocation != null)
            myCurrentLocation.remove();

        myCurrentLocation = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("You")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_current_location)));

        /*if (selectedPlaceMarker == null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));*/

        if (!isUserMoveMap)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15f));
    }

    private void setSelectedPlaceCircle() {
        if (selectedPlaceCircle != null)
            selectedPlaceCircle.remove();

        selectedPlaceCircle = mMap.addCircle(new CircleOptions()
                .center(selectedPlaceMarker.getPosition())
                .radius(selectedPlaceRange)
                .fillColor((int) Long.parseLong(getColorString(randomMarkerColor + "", true), 16))
                .strokeColor((int) Long.parseLong(getColorString(randomMarkerColor + "", false), 16))
                .strokeWidth(2f));

        double kiloMeter = selectedPlaceRange / 1000;
        double mile = kiloMeter * 0.621371;
        rangeDistanceNumberText.setText(getString(R.string.range_distance_number, selectedPlaceRange + "", kiloMeter + "", mile + ""));
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (isGPSOn()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();

                if (myCurrentLocation != null)
                    myCurrentLocation.remove();

                setCurrentLocationMarker();
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
                locationSnackBar = Snackbar.make(frameLayout, R.string.location_dialog_message, Snackbar.LENGTH_LONG);
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
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 30f));
        if (isGPSOn()) {
            setCurrentLocationMarker();

            randomMarkerColor = markerColors[new Random().nextInt(markerColors.length)];
        }

        setMapClickLisnter();
    }

    private void setMapClickLisnter() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                createOwnLocationLayout.setBackgroundResource(R.drawable.background_shape_list_item);

                for (int i = 0; i < mPossibleLocations.size(); i++)
                    if (mPossibleLocations.get(i).isSelected)
                        mPossibleLocations.get(i).isSelected = false;

                mPossibleLocationAdapter.notifyDataSetChanged();

                locationsLayout.setVisibility(View.VISIBLE);

                navNextBtn.setEnabled(true);
                navNextBtn.setClickable(true);
                navNextBtn.setFocusable(true);
                navNextBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

                if (selectedPlaceMarker != null)
                    selectedPlaceMarker.remove();

                selectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Place")
                        .icon(BitmapDescriptorFactory.defaultMarker(randomMarkerColor)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

                try {
                    if (isNetworkAvailable()) {
                        loadNearByPlaces(latLng.latitude, latLng.longitude);

                        errorMessage.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        mPossibleLocationAdapter.clear();
                        scrollMap(-200, 0, 1000);

                        errorMessage.setText(getResources().getString(R.string.no_internet_message));
                        errorMessage.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                isUserMoveMap = true;
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void scrollMap(int x, int y, int millSec) {
        final int xAxis = (isRTL(Locale.getDefault())) ? x *= -1 : x;
        final int yAxis = y;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mMap.animateCamera(CameraUpdateFactory.scrollBy(xAxis, yAxis));
            }
        }, millSec);
    }

    private static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLoctionUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private PossibleLocation getSelectPlaceInfo() {
        for (int i = 0; i < mPossibleLocations.size(); i++) {
            if (mPossibleLocations.get(i).isSelected)
                return mPossibleLocations.get(i);
        }
        return new PossibleLocation();
    }

    String alarmName;

    @OnClick(R.id.create_own_location_layout)
    public void createOwnLocationDefault() {
        createOwnLocationLayout.setBackgroundResource(R.drawable.background_shape_list_item);

        for (int i = 0; i < mPossibleLocations.size(); i++)
            if (mPossibleLocations.get(i).isSelected)
                mPossibleLocations.get(i).isSelected = false;

        if (selectedPlaceMarker != null)
            selectedPlaceMarker.remove();

        selectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                .position(selectedPlaceMarker.getPosition())
                .title("Place")
                .icon(BitmapDescriptorFactory.defaultMarker(randomMarkerColor)));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceMarker.getPosition(), 16f));

        scrollMap(-200, 0, 1000);

        mPossibleLocationAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.nav_next_btn)
    public void onNavNextBtnClicked() {
        if (getSelectPlaceInfo().isSelected) {
            pickPlaceLayout.setVisibility(View.GONE);
            locationsLayout.setVisibility(View.GONE);
            rangeDistanceLayout.setVisibility(View.VISIBLE);

            mMap.setOnMapClickListener(null);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceMarker.getPosition(), 16f));

            newAlarm.setName(getSelectPlaceInfo().getName());
            newAlarm.setPlaceName(getSelectPlaceInfo().getName());
            newAlarm.setVicinity(getSelectPlaceInfo().getVicinity());
            newAlarm.setLatLng(getSelectPlaceInfo().getLatLng());

            setSelectedPlaceCircle();
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.message_location_name_title);

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.message_location_name_ok_button, null);
            builder.setNegativeButton(R.string.message_location_name_cancel_button, null);

            alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button buttonPositive = (alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    buttonPositive.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            alarmName = input.getText().toString();

                            if (!alarmName.equals("")) {
                                newAlarm.setName(alarmName);
                                newAlarm.setPlaceName(alarmName);
                                newAlarm.setLatLng(selectedPlaceMarker.getPosition());

                                pickPlaceLayout.setVisibility(View.GONE);
                                locationsLayout.setVisibility(View.GONE);
                                rangeDistanceLayout.setVisibility(View.VISIBLE);

                                mMap.setOnMapClickListener(null);

                                if (selectedPlaceMarker != null)
                                    selectedPlaceMarker.remove();

                                selectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                                        .position(selectedPlaceMarker.getPosition())
                                        .title(alarmName)
                                        .icon(BitmapDescriptorFactory.defaultMarker(randomMarkerColor)));

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceMarker.getPosition(), 16f));

                                setSelectedPlaceCircle();

                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.edit_activity_must_alarm_name), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Button buttonNegative = (alertDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    buttonNegative.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                }
            });
            alertDialog.show();
        }
    }

    @OnClick({R.id.increament_btn, R.id.decreament_btn, R.id.edit_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.increament_btn:
                if (selectedPlaceRange != 50000)
                    selectedPlaceRange += 100;
                else
                    Toast.makeText(this, "You can't set range more than 50,000 meters.", Toast.LENGTH_SHORT).show();
                setSelectedPlaceCircle();
                break;
            case R.id.decreament_btn:
                if (selectedPlaceRange != 100)
                    selectedPlaceRange -= 100;
                else
                    Toast.makeText(this, "You can't set range less than 100 meters.", Toast.LENGTH_SHORT).show();
                setSelectedPlaceCircle();
                break;
            case R.id.edit_btn:
                showCustomRange();
                break;
        }
    }

    AlertDialog.Builder builder;
    EditText rangeText;
    Spinner unitSpinner;
    View customRangeDistance;
    AlertDialog alertDialog;

    private void showCustomRange() {
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.range_distance);

        customRangeDistance = getLayoutInflater().inflate(R.layout.custom_range_distance, null);
        rangeText = customRangeDistance.findViewById(R.id.range_text);
        unitSpinner = customRangeDistance.findViewById(R.id.unit_spinner);

        rangeText.setText(selectedPlaceRange + "");
        rangeText.selectAll();

        builder.setView(customRangeDistance);

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", null);

        alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button buttonPositive = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (!rangeText.getText().toString().equals("")) {
                            double range = Double.parseDouble(rangeText.getText().toString());
                            String unit = unitSpinner.getSelectedItem().toString();

                            if (unit.equals(getResources().getString(R.string.unit_meters)))
                                range = range;
                            else if (unit.equals(getResources().getString(R.string.unit_kilo_meters)))
                                range *= 1000;
                            else if (unit.equals(getResources().getString(R.string.unit_mile)))
                                range = (range / 0.621371) * 1000;

                            if (range >= 100 && range <= 50000) {
                                selectedPlaceRange = range;
                                alertDialog.dismiss();
                            } else {
                                double defaultMinRange = 100;
                                double defaultMaxRange = 50000;

                                if (unit.equals(getResources().getString(R.string.unit_meters))) {
                                    defaultMinRange = 100;
                                    defaultMaxRange = 50000;
                                } else if (unit.equals(getResources().getString(R.string.unit_kilo_meters))) {
                                    defaultMinRange /= 1000;
                                    defaultMaxRange /= 50000;
                                } else if (unit.equals(getResources().getString(R.string.unit_mile))) {
                                    defaultMinRange = (defaultMinRange / 1000) * 0.621371;
                                    defaultMaxRange = (defaultMaxRange / 1000) * 0.621371;
                                }

                                if (range < 100)
                                    Toast.makeText(alertDialog.getContext(), getResources().getString(R.string.custom_range_distance_minimum_range_error, defaultMinRange + "", unit), Toast.LENGTH_SHORT).show();
                                else if (range > 500000)
                                    Toast.makeText(alertDialog.getContext(), getResources().getString(R.string.custom_range_distance_maximum_range_error, defaultMaxRange + "", unit), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(alertDialog.getContext(), getResources().getString(R.string.custom_range_please_set_range), Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Toast.makeText(builder.getContext(), getResources().getString(R.string.custom_range_please_set_range), Toast.LENGTH_SHORT).show();

                        setSelectedPlaceCircle();
                    }
                });

                Button buttonNegative = (alertDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
            }
        });

        alertDialog.show();
    }

    @OnClick(R.id.nav_set_alarm_btn)
    public void onNavSetAlarmBtnClicked() {
        newAlarm.setRangeDistance(selectedPlaceRange);
        newAlarm.setEnable(true);
        newAlarm.setMarkerColor(randomMarkerColor);
        newAlarm.userCurrentLatLng = mLastLocation;

        newAlarm.isRinging = false;
        newAlarm.isSnoozed = false;
        newAlarm.snoozedRangeDistance = selectedPlaceRange;

        returnNewAlarm();
    }

    private void returnNewAlarm() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("newAlarm", newAlarm);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
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

    private void loadNearByPlaces(double latitude, double longitude) {
        mPossibleLocations.clear();
        //locationsLayout.setVisibility(View.GONE);

        //YOU Can change this type at your own will, e.g hospital, cafe, restaurant.... and see how it all works
        //String type = "grocery_or_supermarket";
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
        //googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&language=").append(LOCALE_LANGUAGE);
        googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        parseLocationResult(result);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void parseLocationResult(JSONObject result) {
        try {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
                scrollMap(-200, 0, 200);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    PossibleLocation PL = new PossibleLocation();

                    if (!place.isNull(ID))
                        PL.setID(place.getString(ID));

                    if (!place.isNull(PLACE_ID))
                        PL.setPlaceID(place.getString(PLACE_ID));

                    if (!place.isNull(NAME))
                        PL.setName(place.getString(NAME));

                    if (!place.isNull(VICINITY))
                        PL.setVicinity(place.getString(VICINITY));

                    double latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LATITUDE);
                    double longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LONGITUDE);

                    PL.setLatLng(new LatLng(latitude, longitude));

                    PL.setReference(place.getString(REFERENCE));
                    PL.setIcon(place.getString(ICON));

                    mPossibleLocations.add(PL);
                }

                mPossibleLocationAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                //locationsLayout.setVisibility(View.VISIBLE);
                //Toast.makeText(getBaseContext(), jsonArray.length() + " Supermarkets found!", Toast.LENGTH_LONG).show();
            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                errorMessage.setText(getResources().getString(R.string.no_places_message));
                errorMessage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }
}
