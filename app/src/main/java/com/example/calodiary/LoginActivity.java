package com.example.calodiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);

        // Ánh xạ các view
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        // Demo đơn giản: kiểm tra username và password
        if (username.equals("admin") && password.equals("admin")) {
            // Lưu trạng thái đăng nhập
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("username", username);
            editor.apply();

            // Chuyển đến màn hình Home
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegister() {
        // TODO: Thêm chức năng đăng ký sau
        Toast.makeText(this, "Chức năng đăng ký sẽ được phát triển sau!", Toast.LENGTH_SHORT).show();
    }
} 