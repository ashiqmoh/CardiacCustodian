package de.hfu.ashiqmoh.cardiaccustodian;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SendToDataLayerThread extends Thread {

    private static final String TAG = "SendToDataLayerThread";

    private GoogleApiClient googleApiClient;
    private String path;
    private int heartRate;

    public SendToDataLayerThread(GoogleApiClient googleApiClient, String path, int heartRate) {
        this.googleApiClient = googleApiClient;
        this.path = path;
        this.heartRate = heartRate;
    }

    @Override
    public void run() {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, Integer.toString(heartRate).getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v(TAG, "Message: {" + heartRate + "} send to: " + node.getDisplayName());
            } else {
                Log.v(TAG, "ERROR: failed to send message");
            }
        }
    }
}
