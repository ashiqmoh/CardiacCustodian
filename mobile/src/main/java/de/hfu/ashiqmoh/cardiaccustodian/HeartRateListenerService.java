package de.hfu.ashiqmoh.cardiaccustodian;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class HeartRateListenerService extends WearableListenerService {

    private static final String TAG = "HeartRateListener";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/heart_rate")) {
            final String heartRate = new String(messageEvent.getData());

            // Log.v(TAG, heartRate);

            // Sends heart rate data to HeartRateMonitorActivity
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra("heartRate", heartRate);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
