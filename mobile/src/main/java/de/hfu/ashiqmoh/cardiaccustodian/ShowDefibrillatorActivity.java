package de.hfu.ashiqmoh.cardiaccustodian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import de.hfu.ashiqmoh.cardiaccustodian.constants.Constants;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Gender;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpMethod;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpOperation;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Usage;
import de.hfu.ashiqmoh.cardiaccustodian.objects.User;

public class ShowDefibrillatorActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>,
        NavigationView.OnNavigationItemSelectedListener,
        HttpTask.Response {

    public static final String TAG = "ShowDefibrillator";

    private SharedPreferences mUserData;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mCurrentLocation;
    private GoogleMap mGoogleMap;

    // variables from notification
    private Usage mUsage;
    private double mDefiLatitude;
    private double mDefiLongitude;
    private double mPatientLatitude;
    private double mPatientLongitude;

    // widgets
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_defibrillator_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUserData = getSharedPreferences(Constants.KEY_USER_DATA, Context.MODE_PRIVATE);

        getDataFromIntent();
        initNavigationDrawer();
        initNavigationView();
        buildMapFragment();
        buildGoogleApiClient();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        Bundle notification = intent.getBundleExtra(Constants.KEY_NOTIFICATION);

        if (notification != null) {

            String usage = notification.getString("usage");
            mUsage = Usage.valueOf(usage);
            Log.v(TAG, "mUsage=" + mUsage);

            String defi = notification.getString("defi");
            Log.v(TAG, "defi:" + defi);

            String patient = notification.getString("patient");
            Log.v(TAG, "patient:" + patient);
            try {
                JSONObject jsonPatient = new JSONObject(patient);
                mPatientLatitude = jsonPatient.getDouble("lat");
                mPatientLongitude = jsonPatient.getDouble("lon");

            } catch (JSONException e) {

    e.printStackTrace();
}
}
        }
    //--- navigation drawer ---//
    private void initNavigationDrawer() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        String firstName = mUserData.getString(Constants.KEY_USER_FIRST_NAME, "");
        String lastName = mUserData.getString(Constants.KEY_USER_LAST_NAME, "");
        String name = firstName + " " + lastName;

        TextView textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_drawer_username);
        textView.setText(name);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_main_map:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_heart_rate_monitor:
                intent = new Intent(this, HeartRateMonitorActivity.class);
                break;
            case R.id.nav_defi_map:
                return true;
            case R.id.nav_first_aid_instruction:
                intent = new Intent(this, FirstAidActivity.class);
                break;
            case R.id.nav_user_profile:
                intent = new Intent(this, UserProfileActivity.class);
                break;
            default:
                intent = null;
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
        return true;
    }

    //--- MapFragment ---//
    private void buildMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.defi_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    //--- GoogleApiClient ---//
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) { }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    //--- LocationSettings ---//
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        buildLocationSettingsRequest();
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        checkLocationSettings();
    }

    private void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                getCurrentLocation();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(ShowDefibrillatorActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getCurrentLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes");
                        break;
                }
        }
    }

    //--- get current location ---//
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.v(TAG, mCurrentLocation.getProvider());

            if (mUsage != null) {

                // if activity opened from notification, show navigation.
                showNavigation();

            } else {

                User user = getUser();
                // String url = "http://141.28.134.124:8080/HelloServlet/Hello";
                // String url = "http://192.168.178.62:8080/HelloServlet/Hello";
                String url = Constants.SERVER_HOST_NAME + "/cc-defi-service/defi";

                // Create json string String using Gson
                Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
                String jsonString = gson.toJson(user);

                new HttpTask(this, url, HttpMethod.POST, HttpOperation.GET_DEFI_LOCATIONS).execute(jsonString);
            }
        }
    }

    //--- get user object ---//
    private User getUser() {
        SharedPreferences mUserData = getSharedPreferences(Constants.KEY_USER_DATA, Context.MODE_PRIVATE);

        String userId = mUserData.getString(Constants.KEY_USER_ID, null);
        Gender gender = mUserData.getInt(Constants.KEY_USER_GENDER, -1) == R.id.radio_option_gender_male ? Gender.M : Gender.W;
        String firstName = mUserData.getString(Constants.KEY_USER_FIRST_NAME, null);
        String lastName = mUserData.getString(Constants.KEY_USER_LAST_NAME, null);

        // TODO: retrieve birthday (json parsing problem at server, excluded)
        // TODO: diseases (gui on xml not implemented yet)

        String helpContact = mUserData.getString(Constants.KEY_USER_HELP_CONTACT, null);

        BigDecimal latitude = BigDecimal.valueOf(mCurrentLocation.getLatitude());
        BigDecimal longitude = BigDecimal.valueOf(mCurrentLocation.getLongitude());
        de.hfu.ashiqmoh.cardiaccustodian.objects.Location location =
                new de.hfu.ashiqmoh.cardiaccustodian.objects.Location(latitude, longitude);

        return new User(userId, gender, firstName, lastName, null, null, helpContact, location);
    }

    @Override
    public void onPostExecute(HttpOperation httpOperation, String result) {
        Log.v(TAG, "result: " + result);

        try {
            JSONArray array = new JSONArray(result);

            Location[] locations = new Location[array.length()];

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = (JSONObject) array.get(i);
                JSONArray loc = (JSONArray) jsonObject.get("loc");

                // Log.v(TAG, "latitude: " + (double) loc.get(0));
                // Log.v(TAG, "longitude: " + (double) loc.get(1));

                locations[i] = new Location("cc" + i);
                locations[i].setLatitude((double) loc.get(1));
                locations[i].setLongitude((double) loc.get(0));
            }

            updateMap(locations);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateMap(Location[] locations) {
        Log.v(TAG, "updateMap");
        if (mGoogleMap != null) {
            Log.v(TAG, "mGoogleMap != null");
            mGoogleMap.clear();

            if (locations.length > 0) {

                for (Location location : locations) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentLatLng = new LatLng(latitude, longitude);
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .title("defi")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.defibrillator_icon))
                    );

                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                    mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                }

            } else {
                Toast.makeText(this, getString(R.string.no_nearby_defibrillator), Toast.LENGTH_LONG).show();
            }

            double currentLatitude = mCurrentLocation.getLatitude();
            double currentLongitude = mCurrentLocation.getLongitude();
            LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        }
    }

    private void showNavigation() {
        Log.v(TAG, "showNavigation");
        if (mGoogleMap == null) {
            return;
        }

        mGoogleMap.clear();

        double latitude = mPatientLatitude;
        double longitude = mPatientLongitude;
        if (mUsage.equals(Usage.DEFI)) {
            latitude = mDefiLatitude;
            longitude = mDefiLongitude;
        }

        LatLng destinationLatLng = new LatLng(latitude, longitude);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(destinationLatLng)
                .title("defi")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.defibrillator_icon))
        );

        double currentLatitude = mCurrentLocation.getLatitude();
        double currentLongitude = mCurrentLocation.getLongitude();
        LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }
}
