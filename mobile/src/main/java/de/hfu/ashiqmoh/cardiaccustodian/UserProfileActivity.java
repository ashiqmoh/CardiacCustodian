package de.hfu.ashiqmoh.cardiaccustodian;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class UserProfileActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UserProfileActivity";

    private static final String KEY_USER_DATA = "shared-preference-user-data";
    private SharedPreferences mUserData;

    private static final String PROFILE_IMAGE_FILENAME = "user_profile_image.png";

    private static final String KEY_USER_EMAIL = "user-email";
    private static final String KEY_USER_FIRST_NAME = "user-first-name";
    private static final String KEY_USER_LAST_NAME = "user-last-name";
    private static final String KEY_USER_DATE_OF_BIRTH = "user-date-of-birth";
    private static final String KEY_USER_MONTH_OF_BIRTH = "user-month-of-birth";
    private static final String KEY_USER_YEAR_OF_BIRTH = "user-year-of-birth";
    private static final String KEY_USER_EMERGENCY_CONTACT = "user-emergency-contact";
    private static final String KEY_USER_GENDER = "user-gender";

    private static final int SELECT_PHOTO = 1;

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;

    private ImageButton mImageButtonProfileImage;
    private EditText mEditTextEmail;
    private EditText mEditTextFirstName;
    private EditText mEditTextLastName;
    private EditText mEditTextDateOfBirth;
    private EditText mEditTextMonthOfBirth;
    private EditText mEditTextYearOfBirth;
    private EditText mEditTextEmergencyContact;
    private RadioGroup mRadioGroupGender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mUserData = getSharedPreferences(KEY_USER_DATA, Context.MODE_PRIVATE);

        initNavigationDrawer();
        initNavigationView();

        loadWidget();
        loadUserProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_user_profile:
                closeSoftKeyboard();
                saveUserProfile();
                Toast.makeText(this, R.string.user_info_saved, Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.nav_main_map:
                intent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_heart_rate_monitor:
                intent = new Intent(this, HeartRateMonitorActivity.class);
                break;
            case R.id.nav_defi_map:
                break;
            case R.id.nav_first_aid_instruction:
                intent = new Intent(this, FirstAidActivity.class);
                break;
            case R.id.nav_user_profile:
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
        loadUserProfile();
    }

    @Override
    public void onPause() {
        saveUserProfile();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        saveBitmap(selectedImage);
                        loadUserProfile();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    private void initNavigationDrawer() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                closeSoftKeyboard();
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadWidget() {
        mImageButtonProfileImage = (ImageButton) findViewById(R.id.image_button_profile_image);
        mEditTextEmail = (EditText) findViewById(R.id.edit_text_email);
        mEditTextFirstName = (EditText) findViewById(R.id.edit_text_first_name);
        mEditTextLastName = (EditText) findViewById(R.id.edit_text_last_name);
        mEditTextDateOfBirth = (EditText) findViewById(R.id.edit_text_date_of_birth);
        mEditTextMonthOfBirth = (EditText) findViewById(R.id.edit_text_month_of_birth);
        mEditTextYearOfBirth = (EditText) findViewById(R.id.edit_text_year_of_birth);
        mEditTextEmergencyContact = (EditText) findViewById(R.id.edit_text_emergency_contact);
        mRadioGroupGender = (RadioGroup) findViewById(R.id.radio_group_gender);

        mImageButtonProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
    }

    private void closeSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadUserProfile() {
        String email = mUserData.getString(KEY_USER_EMAIL, "");
        String firstName = mUserData.getString(KEY_USER_FIRST_NAME, "");
        String lastName = mUserData.getString(KEY_USER_LAST_NAME, "");
        String dateOfBirth = mUserData.getString(KEY_USER_DATE_OF_BIRTH, "");
        String monthOfBirth = mUserData.getString(KEY_USER_MONTH_OF_BIRTH, "");
        String yearOfBirth = mUserData.getString(KEY_USER_YEAR_OF_BIRTH, "");
        String emergencyContact = mUserData.getString(KEY_USER_EMERGENCY_CONTACT, "");
        int gender = mUserData.getInt(KEY_USER_GENDER, -1);

        mEditTextEmail.setText(email);
        mEditTextFirstName.setText(firstName);
        mEditTextLastName.setText(lastName);
        mEditTextDateOfBirth.setText(dateOfBirth);
        mEditTextMonthOfBirth.setText(monthOfBirth);
        mEditTextYearOfBirth.setText(yearOfBirth);
        mEditTextEmergencyContact.setText(emergencyContact);
        mRadioGroupGender.check(gender);

        setProfileImage();
    }

    private void saveUserProfile() {
        String email = mEditTextEmail.getText().toString();
        String firstName = mEditTextFirstName.getText().toString();
        String lastName = mEditTextLastName.getText().toString();
        String dateOfBirth = mEditTextDateOfBirth.getText().toString();
        String monthOfBirth = mEditTextMonthOfBirth.getText().toString();
        String yearOfBirth = mEditTextYearOfBirth.getText().toString();
        String emergencyContact = mEditTextEmergencyContact.getText().toString();
        int gender = mRadioGroupGender.getCheckedRadioButtonId();

        SharedPreferences.Editor editor = mUserData.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_FIRST_NAME, firstName);
        editor.putString(KEY_USER_LAST_NAME, lastName);
        editor.putString(KEY_USER_DATE_OF_BIRTH, dateOfBirth);
        editor.putString(KEY_USER_MONTH_OF_BIRTH, monthOfBirth);
        editor.putString(KEY_USER_YEAR_OF_BIRTH, yearOfBirth);
        editor.putString(KEY_USER_EMERGENCY_CONTACT, emergencyContact);
        editor.putInt(KEY_USER_GENDER, gender);

        editor.commit();
    }

    private void saveBitmap(Bitmap bitmap) {
        String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();

        File file = new File(externalStorageDirectory, PROFILE_IMAGE_FILENAME);
        try {
            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setProfileImage() {
        new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();
                        String filename = externalStorageDirectory + "/" + PROFILE_IMAGE_FILENAME;
                        Bitmap bitmap = null;
                        try {
                            File file = new File(filename);
                            if (!file.exists()) {
                                InputStream inputStream = getAssets().open("unknown_profile_image.png");
                                bitmap = BitmapFactory.decodeStream(inputStream);
                            } else {
                                // bitmap = BitmapFactory.decodeFile(filename);
                                InputStream inputStream = getAssets().open("unknown_profile_image.png");
                                bitmap = BitmapFactory.decodeStream(inputStream);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mImageButtonProfileImage.setImageBitmap(bitmap);
                    }
                });
            }
        }.start();
    }
}
