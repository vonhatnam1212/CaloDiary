package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calodiary.databinding.ActivityLoginBinding;
import com.example.calodiary.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseManager firebaseManager;
    private boolean isPasswordVisible = false;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            checkPendingEmailAndRole(currentUser);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        binding.registerNow.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class));
            finish();
        });

        binding.togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.togglePassword.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                binding.passwordInput.setInputType(InputType.TYPE_CLASS_TEXT);
                binding.togglePassword.setImageResource(R.drawable.ic_visibility);
                isPasswordVisible = true;
            }
            binding.passwordInput.setSelection(binding.passwordInput.getText().length());
        });

        binding.fgpBtn.setOnClickListener(v -> startActivity(new Intent(this, ForgetPassword.class)));

        binding.loginBtn.setOnClickListener(v -> {
            String input = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(input) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (input.contains("@") && input.contains(".")) {
                checkEmailAndLogin(input, password);
            } else {
                firebaseManager.getInstance().db.collection("users")
                        .whereEqualTo("username", input)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                String email = task.getResult().getDocuments().get(0).getString("email");
                                if (email != null) {
                                    performLogin(email, password);
                                } else {
                                    Toast.makeText(this, "Không tìm thấy email liên kết", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Username không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void checkEmailAndLogin(String email, String password) {
        firebaseManager.getInstance().db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String pendingEmail = task.getResult().getDocuments().get(0).getString("pendingEmail");
                        Boolean emailVerified = task.getResult().getDocuments().get(0).getBoolean("emailVerified");
                        if (pendingEmail != null && !pendingEmail.equals(email) && (emailVerified == null || !emailVerified)) {
                            Toast.makeText(this, "Vui lòng xác nhận email mới (" + pendingEmail + ")", Toast.LENGTH_LONG).show();
                        } else {
                            performLogin(email, password);
                        }
                    } else {
                        performLogin(email, password);
                    }
                });
    }

    private void performLogin(String email, String password) {
        firebaseManager.getInstance().mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseManager.getCurrentUser();
                        if (user != null) {
                            checkPendingEmailAndRole(user);
                        }
                    } else {
                        Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkPendingEmailAndRole(FirebaseUser user) {
        firebaseManager.loadUserData(user, userData -> {
            if (userData.getPendingEmail() != null && !userData.getPendingEmail().equals(user.getEmail()) && !user.isEmailVerified()) {
                Toast.makeText(this, "Vui lòng xác nhận email mới (" + userData.getPendingEmail() + ")", Toast.LENGTH_LONG).show();
            } else if (user.isEmailVerified()) {
                redirectBasedOnRole(userData.getRole());
            } else {
                Toast.makeText(this, "Email chưa được xác nhận!", Toast.LENGTH_LONG).show();
            }
        }, this);
    }

    private void redirectBasedOnRole(String role) {
        Intent intent = "admin".equals(role) ?
                new Intent(this, AdminDashboard.class) :
                new Intent(this, HomeActivity.class);
        Toast.makeText(this, "Đăng nhập thành công với vai trò " + (role.equals("admin") ? "Admin" : "User"), Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}