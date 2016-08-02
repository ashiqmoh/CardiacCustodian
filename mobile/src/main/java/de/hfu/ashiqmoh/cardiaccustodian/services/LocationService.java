package de.hfu.ashiqmoh.cardiaccustodian.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import de.hfu.ashiqmoh.cardiaccustodian.HttpTask;
import de.hfu.ashiqmoh.cardiaccustodian.R;
import de.hfu.ashiqmoh.cardiaccustodian.constants.Constants;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Gender;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpMethod;
import de.hfu.ashiqmoh.cardiaccustodian.enums.HttpOperation;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Usage;
import de.hfu.ashiqmoh.cardiaccustodian.objects.CcNotifier;
import de.hfu.ashiqmoh.cardiaccustodian.objects.Defi;
import de.hfu.ashiqmoh.cardiaccustodian.objects.User;

public class LocationService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        HttpTask.Response {

    private static final String TAG = "LocationService";

    private GoogleApiClient mGoogleApiClient;

    private boolean flagGetLocation = false;

    private double mHelperLatitude;
    private double mHelperLongitude;

    private double mPatientLatitude;
    private double mPatientLongitude;

    private String mDefiId;
    private double mDefiLatitude;
    private double mDefiLongitude;

    private double mDistanceHelperPatient;
    private double mDistanceHelperDefi;

    // constructor
    public LocationService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "building google api client");
        buildGoogleApiClient();
    }

    /**
     * Disconnect GoogleApiClient when ending this service.
     */
    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    /**
     * Override onHandleIntent method from super class IntentService.
     * This method is called when this intent is started with startService(intent).
     *
     * This service will retrieve user location when location (GPS) is turned on.
     *
     * @param intent    Intent object with any extras if provided.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        mPatientLatitude = intent.getDoubleExtra(Constants.KEY_LATITUDE, 0.0);
        mPatientLongitude = intent.getDoubleExtra(Constants.KEY_LONGITUDE, 0.0);

        if (!mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            Log.v(TAG, "GoogleApiClient not connected / isConnecting");
            flagGetLocation = true;
            mGoogleApiClient.connect();
        } else {
            Log.v(TAG, "GoogleApiClient connected");
            getLocation();
        }

    }

    /** build GoogleApiClient */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.v(TAG, "connecting to google api client");
        mGoogleApiClient.connect();
    }

    /**
     * Called when successfully connected to GoogleApiClient.
     * Start retrieving the user's location.
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "onConnected");
        if (flagGetLocation) {
            Log.v(TAG, "flagged to get location");
            flagGetLocation = false;
            getLocation();
        } else {
            Log.v(TAG, "not flagged to get location");
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.v(TAG, "onConnectionFailed");
    }

    /** Gets user's last known current location i.e. current location. */
    private void getLocation() {
        Log.v(TAG, "getting location");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mHelperLatitude = location.getLatitude();
            mHelperLongitude = location.getLongitude();
        }
        getDistanceHelperPatient();
    }

    private void sendNotifierRestCall() {

        // get GCM Id of helper
        SharedPreferences mUserData = getSharedPreferences(Constants.KEY_USER_DATA, Context.MODE_PRIVATE);
        String userId = mUserData.getString(Constants.KEY_USER_ID, null);

        // Extract location of patient and pack it as Location object
        de.hfu.ashiqmoh.cardiaccustodian.objects.Location patientLocation = null;
        BigDecimal latitude = BigDecimal.valueOf(mPatientLatitude);
        BigDecimal longitude = BigDecimal.valueOf(mPatientLongitude);
        patientLocation = new de.hfu.ashiqmoh.cardiaccustodian.objects.Location(latitude, longitude);

        // patient
        User user = new User(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                patientLocation
        );

        double[] defiLocation = {mDefiLatitude, mDefiLongitude};
        Defi defi = new Defi("123", defiLocation, "additional_information");

        // Usage.HELPER if helper near to patient
        // Usage.DEFI if helper near to defi
        Usage usage = (mDistanceHelperPatient < mDistanceHelperDefi) ? Usage.HELPER : Usage.DEFI;

        CcNotifier ccNotifier = new CcNotifier(userId, user, defi, usage);

        Gson gson = new Gson();
        String jsonString = gson.toJson(ccNotifier);

        String url = Constants.SERVER_HOST_NAME + "/cc-notifier/notify";

        Log.v(TAG, jsonString);
        new HttpTask(null, url, HttpMethod.POST, HttpOperation.DEFAULT).execute(jsonString);
    }

    private void getDistanceHelperPatient() {
        String googleMatrixUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                + mHelperLatitude + "," + mHelperLongitude
                + "&destinations="
                + mPatientLatitude + "," + mPatientLongitude
                + "&key=AIzaSyDQrBtmnc8DjN4KCTxQb0_32HgiM0nz0QQ";

        new HttpTask(this, googleMatrixUrl, HttpMethod.GET, HttpOperation.GET_DISTANCE_HELPER_PATIENT).execute("");
    }

    private void getDefiLocations() {
        //--- get user object ---//
        SharedPreferences mUserData = getSharedPreferences(Constants.KEY_USER_DATA, Context.MODE_PRIVATE);
        String userId = mUserData.getString(Constants.KEY_USER_ID, null);
        Gender gender = mUserData.getInt(Constants.KEY_USER_GENDER, -1) == R.id.radio_option_gender_male ? Gender.M : Gender.W;
        String firstName = mUserData.getString(Constants.KEY_USER_FIRST_NAME, null);
        String lastName = mUserData.getString(Constants.KEY_USER_LAST_NAME, null);
        // TODO: retrieve birthday (json parsing problem at server, excluded)
        // TODO: diseases (gui on xml not implemented yet)
        String helpContact = mUserData.getString(Constants.KEY_USER_HELP_CONTACT, null);

        BigDecimal latitude = BigDecimal.valueOf(mHelperLatitude);
        BigDecimal longitude = BigDecimal.valueOf(mHelperLongitude);
        de.hfu.ashiqmoh.cardiaccustodian.objects.Location loc =
                new de.hfu.ashiqmoh.cardiaccustodian.objects.Location(latitude, longitude);

        User user = new User(userId, gender, firstName, lastName, null, null, helpContact, loc);

        String url = Constants.SERVER_HOST_NAME + "/cc-defi-service/defi";

        // Create json string String using Gson
        Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
        String jsonString = gson.toJson(user);

        new HttpTask(this, url, HttpMethod.POST, HttpOperation.GET_DEFI_LOCATIONS).execute(jsonString);
    }

    private void getDistanceHelperDefi(String jsonDefi) {

        // Extract defi locations from json response.
        try {
            JSONArray array = new JSONArray(jsonDefi);
            JSONObject jsonObject = (JSONObject) array.get(0);
            JSONArray loc = (JSONArray) jsonObject.get("loc");

            mDefiLatitude = (double) loc.get(1);
            mDefiLongitude = (double) loc.get(0);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        // REST call to Google Distance Matrix Service to get distance between helper and defi
        String googleMatrixUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                + mHelperLatitude + "," + mHelperLongitude
                + "&destinations="
                + mDefiLatitude + "," + mDefiLongitude
                + "&key=AIzaSyDQrBtmnc8DjN4KCTxQb0_32HgiM0nz0QQ";

        new HttpTask(this, googleMatrixUrl, HttpMethod.GET, HttpOperation.GET_DISTANCE_HELPER_DEFI).execute("");
    }


    @Override
    public void onPostExecute(HttpOperation httpOperation, String result) {
        switch (httpOperation) {
            case GET_DISTANCE_HELPER_PATIENT:
                // Log.v(TAG, "distance helper-patient" + result);
                mDistanceHelperPatient = extractDistance(result);
                // Log.v(TAG, "mDistanceHelperPatient="+mDistanceHelperPatient);
                getDefiLocations();
                break;

            case GET_DEFI_LOCATIONS:
                // Log.v(TAG, "defi locations:" + result);
                getDistanceHelperDefi(result);
                break;

            case GET_DISTANCE_HELPER_DEFI:
                // Log.v(TAG, "distance helper-defi" + result);
                mDistanceHelperDefi = extractDistance(result);
                // Log.v(TAG, "mDistanceHelperDefi="+mDistanceHelperDefi);
                sendNotifierRestCall();
                break;

        }

    }

    private double extractDistance(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray rows = jsonObject.getJSONArray("rows");
            JSONObject row0 = rows.getJSONObject(0);
            JSONArray elements = row0.getJSONArray("elements");
            JSONObject element0 = elements.getJSONObject(0);
            JSONObject distance = element0.getJSONObject("distance");
            return distance.getDouble("value");

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
