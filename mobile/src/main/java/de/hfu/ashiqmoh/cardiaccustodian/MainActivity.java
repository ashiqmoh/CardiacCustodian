package de.hfu.ashiqmoh.cardiaccustodian;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;

import de.hfu.ashiqmoh.cardiaccustodian.constants.Constants;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Gender;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpMethod;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpOperation;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Usage;
import de.hfu.ashiqmoh.cardiaccustodian.gcm.GCMRegistrationIntentService;
import de.hfu.ashiqmoh.cardiaccustodian.objects.CcEmergency;
import de.hfu.ashiqmoh.cardiaccustodian.objects.User;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        NavigationView.OnNavigationItemSelectedListener {

    protected static final String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    protected static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected static final String KEY_LOCATION = "location";
    protected static final String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    protected static final String ADDRESS_REQUESTED_KEY = "address-requested-pending";

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    protected String mLastUpdateTime;
    protected GoogleMap mMap;
    protected SupportMapFragment mMapFragment;

    protected boolean mAddressRequested;
    protected String mAddressOutput;
    private AddressResultReceiver mResultReceiver;

    // GCM related variables
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean isGCMReceiverRegistered = false;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private SharedPreferences mUserData;

    private Boolean mInitialMapLoad;
    private static boolean fab_main_checked = false;

    // widget
    private FloatingActionButton mMainFab;
    private FloatingActionButton mCallFab;
    private FloatingActionButton mNaviFab;
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUserData = getSharedPreferences(UserProfileActivity.KEY_USER_DATA, Context.MODE_PRIVATE);

        mResultReceiver = new AddressResultReceiver(new Handler());

        initFloatingActionButtons();
        initNavigationDrawer();
        initNavigationView();
        updateValuesFromBundle(savedInstanceState);
        initGcmReceiver();
        initGcmRegistration();
        buildMapFragment();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
    }

    private void initFloatingActionButtons() {
        mMainFab = (FloatingActionButton) findViewById(R.id.fab_main);
        mCallFab = (FloatingActionButton) findViewById(R.id.fab_call);
        mNaviFab = (FloatingActionButton) findViewById(R.id.fab_navi);

        mMainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fab_main_checked) {
                    mCallFab.setVisibility(FloatingActionButton.INVISIBLE);
                    mNaviFab.setVisibility(FloatingActionButton.INVISIBLE);
                    mMainFab.setImageResource(R.drawable.ic_add_white_24dp);
                    fab_main_checked = false;
                } else {
                    mCallFab.setVisibility(FloatingActionButton.VISIBLE);
                    mNaviFab.setVisibility(FloatingActionButton.VISIBLE);
                    mMainFab.setImageResource(R.drawable.ic_remove_white_24dp);
                    fab_main_checked = true;
                }
            }
        });

        mNaviFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        mCallFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EndCallListener callListener = new EndCallListener(getApplicationContext());
                TelephonyManager mTM = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
                mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

                String url = "http://cardiaccustodian.hs-furtwangen.de:8080/cc-emergency-caller/emergency";

                de.hfu.ashiqmoh.cardiaccustodian.objects.Location loc = null;
                if (mCurrentLocation != null) {
                    BigDecimal latitude = BigDecimal.valueOf(mCurrentLocation.getLatitude());
                    BigDecimal longitude = BigDecimal.valueOf(mCurrentLocation.getLongitude());
                    loc = new de.hfu.ashiqmoh.cardiaccustodian.objects.Location(latitude, longitude);
                }

                String userId = mUserData.getString(Constants.KEY_USER_ID, null);
                String firstName = mUserData.getString(Constants.KEY_USER_FIRST_NAME, null);
                String lastName = mUserData.getString(Constants.KEY_USER_LAST_NAME, null);
                Gender gender = mUserData.getInt(Constants.KEY_USER_GENDER, -1) == R.id.radio_option_gender_male ? Gender.M : Gender.W;
                String helpContact = mUserData.getString(Constants.KEY_USER_HELP_CONTACT, null);

                User user = new User(
                        userId,
                        gender,
                        firstName,
                        lastName,
                        null,
                        null,
                        helpContact,
                        loc
                );

                CcEmergency ccEmergency = new CcEmergency(userId, user, null, Usage.PATIENT);

                Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
                String jsonString = gson.toJson(ccEmergency);
                Log.v(TAG, jsonString);

                new HttpTask(null, url, HttpMethod.POST, HttpOperation.CC_EMERGENCY).execute(jsonString);

                /* Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:1234567890"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                 */
            }
        });
    }

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

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
        }
    }

    private void initGcmReceiver() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            // Called when the broadcast received.
            // We are sending the broadcast from GCMRegistrationIntentService.

            @Override
            public void onReceive(Context context, Intent intent) {
                // If the broadcast has been received with success
                // that means device is registered successfully
                if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    // Getting the registration token from the intent
                    String token = intent.getStringExtra("token");
                    SharedPreferences.Editor editor = mUserData.edit();
                    editor.putString(UserProfileActivity.KEY_USER_ID, token);
                    editor.apply();

                } else if (intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    // If the intent is not with success then display error message.
                    Toast.makeText(getApplicationContext(), "GCM registration error", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void registerGcmReceiver() {
        if (!isGCMReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
            isGCMReceiverRegistered = true;
        }
    }

    private void unregisterGcmReceiver() {
        // Unregister BroadcastReceiver for GCM
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isGCMReceiverRegistered = false;
    }

    private void initGcmRegistration() {
        // check whether Play Service is available in this device.
        if (checkPlayService()) {
            // If Play Service is available, start intent to register device.
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            startService(intent);
        }
    }

    private boolean checkPlayService() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported");
                // close the activity
                this.finish();
            }
            return false;
        }
        return true;
    }

    protected void buildMapFragment() {
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
            mInitialMapLoad = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mCurrentLocation != null) {
            updateMap();
        }
    }

    private void updateMap() {
        if (mCurrentLocation != null) {
            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();
            LatLng currentLatLng = new LatLng(latitude, longitude);

            if (mMap != null) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("current location"));

                if (mInitialMapLoad) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    mInitialMapLoad = false;
                }
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
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
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(MainActivity.this,REQUEST_CHECK_SETTINGS);
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
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes");
                        break;
                }
        }
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    // do nothing
                }
            });
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                // do nothing
            }
        });
    }

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateMap();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateMap();
        startIntentService(location);
        Toast.makeText(this, "Location updated", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerGcmReceiver();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterGcmReceiver();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            mAddressRequested = false;
            final Snackbar snackBar = Snackbar.make(findViewById(R.id.drawer_layout), mAddressOutput, 2000);
            snackBar.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackBar.dismiss();
                }
            });
            snackBar.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_main_map:
                return true;
            case R.id.nav_heart_rate_monitor:
                intent = new Intent(this, HeartRateMonitorActivity.class);
                break;
            case R.id.nav_defi_map:
                intent = new Intent(this, ShowDefibrillatorActivity.class);
                break;
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
}
