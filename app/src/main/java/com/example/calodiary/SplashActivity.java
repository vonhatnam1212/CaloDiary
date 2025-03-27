package com.example.calodiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginAndNavigate();
            }
        }, SPLASH_DURATION);
    }

    private void checkLoginAndNavigate() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");

        Intent intent;
        if (isLoggedIn && !username.isEmpty()) {
            // Nếu đã đăng nhập và có username, chuyển đến HomeActivity
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            // Nếu chưa đăng nhập hoặc không có username, xóa dữ liệu đăng nhập và chuyển đến LoginActivity
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            intent = new Intent(SplashActivity.this, Login.class);
        }
        
        // Xóa activity stack cũ
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}