package de.hfu.ashiqmoh.cardiaccustodian;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.hfu.ashiqmoh.cardiaccustodian.constants.Constants;

public class HeartRateMonitorActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HeartRateMonitor";

    private SharedPreferences mUserData;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    CoordinatorLayout mCoordinatorLayout;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUserData = getSharedPreferences(Constants.KEY_USER_DATA, Context.MODE_PRIVATE);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_heart_rate);
        mTextView = (TextView) findViewById(R.id.text_view_heart_rate);

        initNavigationDrawer();
        initNavigationView();
        checkBluetoothConnectivity();
    }

    private void checkBluetoothConnectivity() {
        Log.v(TAG, "checkBluetoothConnectivity()");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v(TAG, mBluetoothAdapter.toString());
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            String message = getString(R.string.bluetooth_not_supported);
            showErrorMessage(message);
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                // bluetooth enabled, initialize broadcast receiver to receive heart rate measurements
                initBroadcastReceiver();
            } else {
                // bluetooth not enabled, request user permission through request intent
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    // Bluetooth is now enabled, initialize broadcast receiver to receive
                    // heart rate measurements.
                    initBroadcastReceiver();
                    // show message that bluetooth enabled
                    Snackbar.make(mCoordinatorLayout, getString(R.string.bluetooth_enabled), Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    // User refused to enable bluetooth, show error message
                    String message = getString(R.string.heart_rate_cannot_monitored);
                    showErrorMessage(message);
                }
        }
    }

    private void initBroadcastReceiver() {
        // Implements BroadcastReceiver to receive heart rate data from HeartRateListenerService
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);
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

    @Override
    public void onBackPressed() {
        // if navigation drawer is opened, close navigation drawer onBackPress
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
                return true;
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

    private void showErrorMessage(String message) {
        // show error message through Snackbar if bluetooth is not supported or enabled.
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_heart_rate);
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.snackbar_dismiss), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    class MessageReceiver extends BroadcastReceiver {
        // Heart rate data will be received here from HeartRateListenerService
        @Override
        public void onReceive(Context context, Intent intent) {
            String heartRate = intent.getStringExtra("heartRate");
            Log.v(TAG, heartRate);
            mTextView.setText(heartRate);
        }
    }
}
