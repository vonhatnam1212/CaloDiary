package com.example.calodiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    EditText editTextFullName, editTextUsername, editTextEmail, editTextDob, editTextGender,
            editTextHeight, editTextWeight;
    Button editButton, logoutButton;
    BottomNavigationView bottomNavigationView;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        editTextFullName = findViewById(R.id.fullname_input);
        editTextUsername = findViewById(R.id.username1_input);
        editTextEmail = findViewById(R.id.email_input);
        editTextDob = findViewById(R.id.dob_input);
        editTextGender = findViewById(R.id.gender_input);
        editTextHeight = findViewById(R.id.height_input);
        editTextWeight = findViewById(R.id.weight_input);
        editButton = findViewById(R.id.edit_btn);
        logoutButton = findViewById(R.id.logout_btn);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        editTextFullName.setEnabled(false);
        editTextUsername.setEnabled(false);
        editTextEmail.setEnabled(false);
        editTextDob.setEnabled(false);
        editTextGender.setEnabled(false);
        editTextHeight.setEnabled(false);
        editTextWeight.setEnabled(false);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return;
        }

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(Profile.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        editButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
            builder.setTitle("Enter Current Password");

            final EditText input = new EditText(Profile.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setHint("Your current password");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String password = input.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Profile.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(user.getEmail(), password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                                    startActivityForResult(intent, 1); // Gọi EditProfile với requestCode
                                } else {
                                    Toast.makeText(Profile.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home || itemId == R.id.blog) {
                Toast.makeText(Profile.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.profile) {
                return true;
            }
            return false;
        });

        loadUserData(); // Load dữ liệu ban đầu
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Reload dữ liệu mỗi khi quay lại màn hình
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserData(); // Reload dữ liệu khi EditProfile trả về kết quả thành công
        }
    }

    private void loadUserData() {
        if (user != null) {
            editTextEmail.setText(user.getEmail()); // Lấy email từ FirebaseAuth
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Log.d("Profile", "User data: " + document.getData().toString());
                                editTextFullName.setText(document.getString("fullName") != null ? document.getString("fullName") : "Not set");
                                editTextUsername.setText(document.getString("username") != null ? document.getString("username") : "Not set");
                                editTextDob.setText(document.getString("dob") != null ? document.getString("dob") : "Not set");
                                editTextGender.setText(document.getString("gender") != null ? document.getString("gender") : "Not set");
                                editTextHeight.setText(document.getString("height") != null ? document.getString("height") + " cm" : "Not set");
                                editTextWeight.setText(document.getString("weight") != null ? document.getString("weight") + " kg" : "Not set");
                            } else {
                                Toast.makeText(Profile.this, "No user data found in Firestore", Toast.LENGTH_SHORT).show();
                                resetFields();
                            }
                        } else {
                            Toast.makeText(Profile.this, "Failed to fetch data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("Profile", "Error fetching data", task.getException());
                            resetFields();
                        }
                    });
        }
    }

    private void resetFields() {
        editTextFullName.setText("Not set");
        editTextUsername.setText("Not set");
        editTextDob.setText("Not set");
        editTextGender.setText("Not set");
        editTextHeight.setText("Not set");
        editTextWeight.setText("Not set");
    }
}