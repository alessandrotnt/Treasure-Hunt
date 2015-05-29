package com.example.treasurehunt;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;

/**
* <p>MMNET Team 04</p>
* <p>Project Title: Treasure Hunt</p>
* <p>Class Description: This activity simply shows our application's cover
* @authors Alessandro Tontini & Martina Valente
*/

public class SplashActivity extends FragmentActivity {
    private static boolean splashLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!splashLoaded) {
            setContentView(R.layout.activity_splash);
            splashLoaded = true;
            
            int secondsDelayed = 2;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startActivity(new Intent(SplashActivity.this, BluetoothActivity.class));
                    finish();
                }
            }, secondsDelayed * 500);
        }
        else {
            Intent goToMainActivity = new Intent(SplashActivity.this, BluetoothActivity.class);
            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToMainActivity);
            finish();
        }
    }
}
