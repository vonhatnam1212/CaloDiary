package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgetPassword extends AppCompatActivity {

    EditText editTextEmailOrUsername;
    Button buttonReset;
    ImageButton backButton;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmailOrUsername = findViewById(R.id.fullname_input); // Sử dụng id từ layout của bạn
        buttonReset = findViewById(R.id.login_btn); // Nút "reset"
        backButton = findViewById(R.id.back_button);

        // Nút quay lại
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        // Nút Reset
        buttonReset.setOnClickListener(v -> {
            String input = editTextEmailOrUsername.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(ForgetPassword.this, "Vui lòng nhập email hoặc username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (input.contains("@") && input.contains(".")) {
                // Nếu là email, gửi link đặt lại mật khẩu trực tiếp
                sendPasswordResetEmail(input);
            } else {
                // Nếu là username, tra cứu email từ Firestore
                db.collection("users")
                        .whereEqualTo("username", input)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                String foundEmail = task.getResult().getDocuments().get(0).getString("email");
                                if (foundEmail != null) {
                                    sendPasswordResetEmail(foundEmail);
                                } else {
                                    Toast.makeText(ForgetPassword.this, "Không tìm thấy email liên kết với username này", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ForgetPassword.this, "Username không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ForgetPassword.this, "Lỗi khi tra cứu username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgetPassword.this, "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ForgetPassword.this, "Lỗi khi gửi email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}