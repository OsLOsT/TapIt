
package com.oslost.tapit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    /* onSearch Variable */
    private EditText onSearch;
    private TextView displayAddress;
    private String location;
    private SharedPreferences showLocation;
    private Context context;

    /* #################################################################
        SharedPrefs showLocation contains the following key-value pair:

        - MyLocationName : <Location Name>
        - MyLatitude : <Location Lat>
        - MyLongitude : <Location Lng>

       ################################################################# */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /* Bottom Navigation View with Activity */
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        /* Add a listner for Button Updates */
        findViewById(R.id.updateAddress).setOnClickListener(updateMyAddress);

        onSearch = findViewById(R.id.editAddress);
        displayAddress = findViewById(R.id.showAddress);

        displayTextAddress();

    }

    /* #################################################
        SHARED PREFERENCES SAVING AND LOADING FUNCTIONS
       ################################################# */

    public void savePrefs(String key, float value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public void savePrefs(String key, String value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private float loadPrefs(String key, float value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        float data = showLocation.getFloat(key, value);
        return data;
    }

    private String loadPrefs(String key, String value){
       showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String data = showLocation.getString(key, value);
        return data;
    }

     /* ##################################
            DISPLAY ADDRESS TEXT FUNCTION
        ##################################*/

    private void displayTextAddress() {
        String tempDisplay = loadPrefs("MyLocationName", null);
        if(tempDisplay != null){
            onSearch.setText(tempDisplay) ;
        }
    }

  /* #################################################
                ON CLICK LISTENER FUNCTIONS
     #################################################*/

    private View.OnClickListener updateMyAddress = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onSearchLocation(v);
        }
    };


    /* ###########################
            GEOCODE FUNCTION
       ###########################*/

    public void onSearchLocation(View view) {

        location = onSearch.getText().toString();
        savePrefs("MyLocationName", onSearch.getText().toString().toUpperCase());
        if (onSearch != null) {
            List<Address> addressList = null;
            if (location != null || !location.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {

                    addressList = geocoder.getFromLocationName(location, 1);
                    if (addressList != null) {
                        addressList = geocoder.getFromLocationName(location, 1);

                    } else {
                        //addressList = null;
                        Toast.makeText(this, "Location does not exist,please enter something else.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Location does not exist,please enter something else.", Toast.LENGTH_SHORT).show();


                }
                if (addressList.size() > 0) {

                    Address address = addressList.get(0);
                    String show = "Latitude: " + address.getLatitude() + "\nLongitude: " + address.getLongitude();
                    displayAddress.setText(show);
                    Toast.makeText(getApplicationContext(), address.getLatitude() + " " + address.getLongitude(), Toast.LENGTH_LONG).show();

                    /* Store the updated address into the static variable */
                    showLocation.edit().putString("MyLatitude", String.valueOf(address.getLatitude())).apply();
                    showLocation.edit().putString("MyLongitude", String.valueOf(address.getLongitude())).apply();

                } else {
                    Toast.makeText(this, "Location does not exist, please enter something else.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }




    /* ############################################
            BOTTOM NAVIGATION VIEW FUNCTION
       ############################################*/

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //Activity selectedActivity = null;

                    switch (item.getItemId()) {
                        case R.id.nav_map:
                            Intent imap = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(imap);
                            break;
                        case R.id.nav_ai:
                            Intent iai = new Intent(getApplicationContext(), AiActivity.class);
                            startActivity(iai);
                            break;
                        case R.id.nav_settings:
                            break;
                    }

                    return true;
                }
            };

}