package com.firebasechat.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import com.firebasechat.R;
import com.firebasechat.controller.ConstantData;

public class SplashScreen extends AppCompatActivity {

    //Global variable
    SharedPreferences pref;
    private static final int SPLASH_DISPLAY_TIME = 1000 * 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        pref = getSharedPreferences(ConstantData.PREFERENCES, MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                boolean login = pref.getBoolean("login", false);
                if (!login) {
                    startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                } else {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                }
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_TIME);
    }
}
