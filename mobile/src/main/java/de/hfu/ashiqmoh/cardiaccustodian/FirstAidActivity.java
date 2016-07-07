package de.hfu.ashiqmoh.cardiaccustodian;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;

public class FirstAidActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FirstAidActivity";

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private MediaPlayer mMediaPlayer;

    private static String KEY_CURRENT_POSITION = "current-position";
    private static String KEY_IS_PLAYING = "is-playing";
    private int mCurrentPosition = 0;
    private boolean mIsPlaying = false;

    protected static boolean mBackToMain = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_aid_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        updateValuesFromBundle(savedInstanceState);
        initMediaPlayer();
        initNavigationDrawer();
        initNavigationView();
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_CURRENT_POSITION)) {
                mCurrentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
            }
            if (savedInstanceState.keySet().contains(KEY_IS_PLAYING)) {
                mIsPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING);
            }
        }
    }

    private void initMediaPlayer() {
        try {
            AssetFileDescriptor mRhythm = getAssets().openFd("rhythm.mp3");

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setDataSource(mRhythm.getFileDescriptor(),
                    mRhythm.getStartOffset(),
                    mRhythm.getLength());
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        if (mBackToMain) {
            mBackToMain = false;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_first_aid, menu);
        if (mIsPlaying) {
            menu.findItem(R.id.play_reanimation_rhythm).setVisible(false);
            menu.findItem(R.id.pause_reanimation_rhythm).setVisible(true);
        } else {
            menu.findItem(R.id.play_reanimation_rhythm).setVisible(true);
            menu.findItem(R.id.pause_reanimation_rhythm).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.play_reanimation_rhythm:
                mMediaPlayer.start();
                invalidateOptionsMenu();
                mIsPlaying = true;
                return true;
            case R.id.pause_reanimation_rhythm:
                mMediaPlayer.pause();
                invalidateOptionsMenu();
                mIsPlaying = false;
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                return true;
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

    @Override
    public void onResume() {
        super.onResume();
        if (mIsPlaying) {
            mMediaPlayer.seekTo(mCurrentPosition);
            mMediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        mCurrentPosition = mMediaPlayer.getCurrentPosition();
        if (mMediaPlayer.isPlaying()) {
            mIsPlaying = true;
            mMediaPlayer.stop();
        } else {
            mIsPlaying = false;
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(KEY_CURRENT_POSITION, mCurrentPosition);
        savedInstanceState.putBoolean(KEY_IS_PLAYING, mIsPlaying);
        super.onSaveInstanceState(savedInstanceState);
    }
}
