package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calodiary.databinding.ActivityForgetPasswordBinding;
import com.example.calodiary.utils.FirebaseManager;

public class ForgetPassword extends AppCompatActivity {
    private ActivityForgetPasswordBinding binding;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        binding.backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });

        binding.loginBtn.setOnClickListener(v -> {
            String input = binding.fullnameInput.getText().toString().trim();
            if (TextUtils.isEmpty(input)) {
                Toast.makeText(this, "Vui lòng nhập email hoặc username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (input.contains("@") && input.contains(".")) {
                firebaseManager.sendPasswordResetEmail(input, this, () -> {
                    startActivity(new Intent(this, Login.class));
                    finish();
                });
            } else {
                firebaseManager.getInstance().getInstance().db.collection("users")
                        .whereEqualTo("username", input)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                String email = task.getResult().getDocuments().get(0).getString("email");
                                if (email != null) {
                                    firebaseManager.sendPasswordResetEmail(email, this, () -> {
                                        startActivity(new Intent(this, Login.class));
                                        finish();
                                    });
                                } else {
                                    Toast.makeText(this, "Không tìm thấy email liên kết với username này", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Username không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}