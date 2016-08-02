package de.hfu.ashiqmoh.cardiaccustodian.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import de.hfu.ashiqmoh.cardiaccustodian.R;
import de.hfu.ashiqmoh.cardiaccustodian.ShowDefibrillatorActivity;
import de.hfu.ashiqmoh.cardiaccustodian.constants.Constants;
import de.hfu.ashiqmoh.cardiaccustodian.enums.Usage;
import de.hfu.ashiqmoh.cardiaccustodian.services.LocationService;

// Class is extending GcmListenerService
public class GCMPushReceiverService extends GcmListenerService {

    private static final String TAG = "GCMPushReceiver";

    private double mLatitude;
    private double mLongitude;

    /**
     * Called when message is received.
     *
     * @param from    SenderID of the sender.
     * @param data    Data bundle containing message data as key/value pairs.
     *                To list out set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {

        Log.v(TAG, "onMessageReceived");

        for (String key : data.keySet()) {
            Log.v(TAG, "keys: " + key);
        }

        Bundle notification = data.getBundle("notification");
        if (notification != null) {
            for (String key : notification.keySet()) {
                Log.v(TAG, "keys: " + key);
            }

            // The received message is shown to the user as a notification.
            sendNotification(notification);
        } else {

            // If Bundle notification is null, message is received from /topics/helper
            String patient = data.getString("patient");

            Log.v(TAG, patient);
            if (patient != null) {

                try {
                    JSONObject patientLocation = new JSONObject(patient);

                    String lat = patientLocation.getString("lat");
                    String lon = patientLocation.getString("lon");

                    mLatitude = Double.parseDouble(lat);
                    mLongitude = Double.parseDouble(lon);

                    Log.v(TAG, "patient latitude:" + mLatitude);
                    Log.v(TAG, "patient longitude:" + mLongitude);

                    // Start location service in background to get current location of the helper.
                    Log.v(TAG, "starting location service");
                    startLocationService(mLatitude, mLongitude);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Create and show a notification containing the received GCM message.
     *
     * @param notification  Bundle notification received from service.
     */
    private void sendNotification(Bundle notification) {
        String title = notification.getString("title");
        String message = notification.getString("body");

        double latitude = 0;
        double longitude = 0;

        String defi = notification.getString("defi");
        Log.v(TAG, "defi:" + defi);

        String patient = notification.getString("patient");
        Log.v(TAG, "patient:" + patient);

        // String usage = (notification.getString("usage").equals("DEFI")) ? "defi" : "patient";
        String usage = notification.getString("usage");
        String destination = null;
        String lat = "lat";
        String lon = "lon";
        if (usage != null) {
            destination = usage.equals("DEFI") ? "defi" : "patient";
            lat = "lon";
            lon = "lat";
        }
        String json = notification.getString(destination);
        Log.v(TAG, "json="+json);
        try {
            JSONObject location = new JSONObject(json);
            latitude = location.getDouble(lat);
            longitude = location.getDouble(lon);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "latitude=" + latitude);
        Log.v(TAG, "longitude=" + longitude);

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, mapIntent, PendingIntent.FLAG_ONE_SHOT);

        /*
        // Set which activity should be opened when user touch the notification.
        Intent intent = new Intent(this, ShowDefibrillatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.KEY_NOTIFICATION, notification);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
*/

        // Sets ringtone for the notification.
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        // Build notification which all the settings.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent);


        // Get notification manager from system.
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);


        // Shows notification.
        // 0 = ID of notification
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void startLocationService(double latitude, double longitude) {
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra(Constants.KEY_LATITUDE, latitude);
        intent.putExtra(Constants.KEY_LONGITUDE, longitude);
        startService(intent);
    }

}
