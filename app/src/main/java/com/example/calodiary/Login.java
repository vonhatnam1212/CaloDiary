package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    EditText editTextEmailOrUsername, editTextPassword;
    Button buttonLog;
    TextView forgotPasswordBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Button buttonNow;
    ImageView togglePassword;
    boolean isPasswordVisible = false;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            checkPendingEmailAndRole(currentUser);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editTextEmailOrUsername = findViewById(R.id.username_input);
        editTextPassword = findViewById(R.id.password_input);
        buttonLog = findViewById(R.id.login_btn);
        buttonNow = findViewById(R.id.registerNow);
        togglePassword = findViewById(R.id.toggle_password);
        forgotPasswordBtn = findViewById(R.id.fgp_btn);

        buttonNow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                togglePassword.setImageResource(R.drawable.ic_visibility);
                isPasswordVisible = true;
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        forgotPasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgetPassword.class);
            startActivity(intent);
        });

        buttonLog.setOnClickListener(v -> {
            String input = editTextEmailOrUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(Login.this, "Vui lòng nhập email hoặc username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (input.contains("@") && input.contains(".")) {
                checkEmailAndLogin(input, password);
            } else {
                db.collection("users")
                        .whereEqualTo("username", input)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                String foundEmail = task.getResult().getDocuments().get(0).getString("email");
                                String pendingEmail = task.getResult().getDocuments().get(0).getString("pendingEmail");
                                Boolean emailVerified = task.getResult().getDocuments().get(0).getBoolean("emailVerified");

                                if (foundEmail != null) {
                                    if (pendingEmail != null && !pendingEmail.equals(foundEmail) && (emailVerified == null || !emailVerified)) {
                                        Toast.makeText(Login.this, "Vui lòng xác nhận email mới (" + pendingEmail + ") trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                                    } else {
                                        performLogin(foundEmail, password);
                                    }
                                } else {
                                    Toast.makeText(Login.this, "Không tìm thấy email liên kết với username này", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(Login.this, "Username không tồn tại", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Login.this, "Lỗi khi tra cứu username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    private void checkEmailAndLogin(String email, String password) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String pendingEmail = task.getResult().getDocuments().get(0).getString("pendingEmail");
                        Boolean emailVerified = task.getResult().getDocuments().get(0).getBoolean("emailVerified");

                        if (pendingEmail != null && !pendingEmail.equals(email) && (emailVerified == null || !emailVerified)) {
                            Toast.makeText(Login.this, "Vui lòng xác nhận email mới (" + pendingEmail + ") trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                        } else {
                            performLogin(email, password);
                        }
                    } else {
                        performLogin(email, password);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Login.this, "Lỗi khi kiểm tra email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void performLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkPendingEmailAndRole(user);
                            }
                        } else {
                            Toast.makeText(Login.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkPendingEmailAndRole(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String pendingEmail = document.getString("pendingEmail");
                        Boolean emailVerified = document.getBoolean("emailVerified");
                        String role = document.getString("role"); // Lấy role từ Firestore

                        if (pendingEmail != null && !pendingEmail.equals(user.getEmail()) && (emailVerified == null || !emailVerified)) {
                            Toast.makeText(Login.this, "Vui lòng xác nhận email mới (" + pendingEmail + ") trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                        } else if (pendingEmail != null && !pendingEmail.equals(user.getEmail()) && user.isEmailVerified()) {
                            // Email mới đã được xác nhận, đồng bộ Firestore
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("email", pendingEmail);
                            updates.put("pendingEmail", null);
                            updates.put("emailVerified", true);

                            db.collection("users").document(user.getUid())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        redirectBasedOnRole(role); // Chuyển hướng dựa trên role
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Login.this, "Lỗi khi cập nhật email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                    });
                        } else if (user.isEmailVerified()) {
                            redirectBasedOnRole(role); // Chuyển hướng dựa trên role
                        } else {
                            Toast.makeText(Login.this, "Email chưa được xác nhận. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Login.this, "Lỗi khi kiểm tra dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void redirectBasedOnRole(String role) {
        if ("admin".equals(role)) {
            Toast.makeText(Login.this, "Đăng nhập thành công với vai trò Admin", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), AdminDashboard.class); // Màn hình Admin
            startActivity(intent);
            finish();
        } else { // Mặc định là "user" hoặc role không xác định
            Toast.makeText(Login.this, "Đăng nhập thành công với vai trò User", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Profile.class); // Màn hình User
            startActivity(intent);
            finish();
        }
    }
}