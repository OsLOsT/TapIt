package com.oslost.tapit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

public class SpashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);

        /* Music Portion of the splash screen */
        MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.introtone);
        mp.start(); //Starts your sound

        finish();
    }
}
