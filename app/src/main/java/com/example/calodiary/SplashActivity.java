package com.example.calodiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().getDecorView().post(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(this, HomeActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        });
    }
}