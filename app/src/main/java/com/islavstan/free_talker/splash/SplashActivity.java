package com.islavstan.free_talker.splash;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.islavstan.free_talker.main.MainActivity;
import com.islavstan.free_talker.registration.RegistrationActivity;
import com.islavstan.free_talker.utils.PreferenceHelper;

public class SplashActivity extends AppCompatActivity {
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());

        if (preferenceHelper.hasQbUser()) {
            MainActivity.startActivity(this);
            finish();
        } else {
            RegistrationActivity.startActivity(this);
            finish();
        }

    }
}
