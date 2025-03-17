package com.example.calodiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginBtn;
    private Button regisBtn;
    private TextView fgpBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo SharedPreferences
        sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);

        // Kiểm tra trạng thái đăng nhập
        if (checkLoginState()) {
            navigateToHome();
            return;
        }

        // Khởi tạo views
        initializeViews();
        setupClickListeners();
    }

    private boolean checkLoginState() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");
        return isLoggedIn && !username.isEmpty();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginBtn = findViewById(R.id.login_btn);
        regisBtn = findViewById(R.id.regis_btn);
        fgpBtn = findViewById(R.id.fgp_btn);
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        regisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement register functionality
                Toast.makeText(LoginActivity.this, "Chức năng đăng ký đang được phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        fgpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement forgot password functionality
                Toast.makeText(LoginActivity.this, "Chức năng quên mật khẩu đang được phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (!validateInput(username, password)) {
            return;
        }

        // Kiểm tra thông tin đăng nhập
        if (checkCredentials(username, password)) {
            // Lưu trạng thái đăng nhập
            saveLoginState(username);
            // Chuyển đến màn hình Home
            navigateToHome();
        } else {
            showError("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    private boolean validateInput(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Vui lòng nhập tên đăng nhập");
            usernameInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkCredentials(String username, String password) {
        // TODO: Implement actual authentication logic
        return username.equals("admin") && password.equals("admin");
    }

    private void saveLoginState(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Ngăn người dùng quay lại màn hình trước đó
        super.onBackPressed();
        moveTaskToBack(true);
    }
} 