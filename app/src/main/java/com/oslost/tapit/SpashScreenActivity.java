package com.oslost.tapit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

public class SpashScreenActivity extends AppCompatActivity {

    /* Splash screen timer */
    private static int SPLASH_TIME_OUT = 2000;
    private static final String TAG = SpashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SpashScreenActivity.this, MapsActivity.class);

                /* Music Portion of the splash screen */
                MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.introtone);
                mp.start(); //Starts your sound

                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}
