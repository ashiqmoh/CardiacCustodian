package de.hfu.ashiqmoh.cardiaccustodian.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class GCMRegistrationIntentService extends IntentService {

    private static final String TAG = "GCMRegistration";

    // Constants for success and errors
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";

    /**
     * PROJECT_ID with GCM module registered at Google Developer console
     * @link {https://console.developers.google.com/}
     */
    public static final String PROJECT_ID = "1087420843285";

    /** array of string with topics, that client should subscribe */
    private static final String[] TOPICS = {"helper"};

    // Class constructor
    public GCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Registering gcm to the device
        registerGCM();
    }

    private void registerGCM() {
        // Registration complete intent initially null
        Intent registrationComplete = null;

        // Register token is also null
        // We will get the token on successful registration
        String token = null;
        try {
            // Creating an instanceID
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

            // Getting the token from the instance id
            token = instanceID.getToken(PROJECT_ID, "GCM");

            // Displaying the token in the log so that we can copy it to send push notification
            // You can also extend the app by storing the token in to your server
            Log.v(TAG, "token:" + token);


            // Subscribe to topic channels
            subscribeTopics(token);

            // on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

            // Putting the token to the intent
            registrationComplete.putExtra("token", token);
        } catch (Exception e) {
            // If any error occurred
            Log.w("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        // Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constants.
     *
     * @param token    GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
