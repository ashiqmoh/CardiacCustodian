package de.hfu.ashiqmoh.cardiaccustodian;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends WearableActivity implements
        SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private TextView mTextView;
    private ImageButton btnStart;
    private ImageButton btnPause;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.heartRateText);
                btnStart = (ImageButton) stub.findViewById(R.id.btnStart);
                btnPause = (ImageButton) stub.findViewById(R.id.btnPause);

                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnStart.setVisibility(ImageButton.GONE);
                        btnPause.setVisibility(ImageButton.VISIBLE);
                        mTextView.setText(R.string.please_wait_msg);
                        startMeasurement();
                    }
                });

                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPause.setVisibility(ImageButton.GONE);
                        btnStart.setVisibility(ImageButton.VISIBLE);
                        mTextView.setText("--");
                        stopMeasurement();
                    }
                });

            }
        });
        setAmbientEnabled();
        buildGoogleApiClient();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager != null) {
            mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        } else {
            Log.i(TAG, "Sensor manager can't be retrieved");
        }

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void startMeasurement() {
        if (mHeartRateSensor != null) {
            boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Sensor " + mHeartRateSensor.getName() + " registered: " + sensorRegistered);
        } else {
            Log.d(TAG, "Heart rate sensor not found");
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    private void stopMeasurement() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float mHeartRateFloat = event.values[0];
            int mHeartRate = Math.round(mHeartRateFloat);
            Log.i(TAG, "mHeartRate=" + mHeartRate);
            mTextView.setText(getString(R.string.heart_rate_measurement, mHeartRate));
            new SendToDataLayerThread(mGoogleApiClient, "/heart_rate", mHeartRate).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.i("Sensor list", sensor.getName() + ": " + sensor.getType());
        }

        Log.i(TAG, "SENSOR_DELAY_NORMAL=" + SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(TAG, "SENSOR_DELAY_FASTEST=" + SensorManager.SENSOR_DELAY_FASTEST);

        Log.i(TAG, "SENSOR_STATUS_ACCURACY_LOW=" + SensorManager.SENSOR_STATUS_ACCURACY_LOW);
        Log.i(TAG, "SENSOR_STATUS_ACCURACY_MEDIUM=" + SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
        Log.i(TAG, "SENSOR_STATUS_ACCURACY_HIGH=" + SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopMeasurement();
        super.onDestroy();
    }
}
