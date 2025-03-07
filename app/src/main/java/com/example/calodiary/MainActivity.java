package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add a delay to show splash screen (you can remove this if not needed)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is logged in here
                boolean isLoggedIn = false; // TODO: Implement your login check logic

                if (isLoggedIn) {
                    // Navigate to Home/Dashboard
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // Navigate to Login
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish(); // This prevents going back to the splash screen
            }
        }, 1000); // 1 second delay, adjust as needed
    }
}