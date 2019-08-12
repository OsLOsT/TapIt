
package com.oslost.tapit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

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
    }

    /* ############################################
            BOTTOM NAVIGATION VIEW FUNCTION
       ############################################*/

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //Activity selectedActivity = null;

                    switch(item.getItemId()) {
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