package com.example.calodiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calodiary.databinding.ActivityProfileBinding;
import com.example.calodiary.model.User;
import com.example.calodiary.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();
        FirebaseUser user = firebaseManager.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        binding.logoutBtn.setOnClickListener(v -> {
            firebaseManager.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.editBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Current Password");
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setHint("Your current password");
            builder.setView(input);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String password = input.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else {
                    firebaseManager.getInstance().mAuth.signInWithEmailAndPassword(user.getEmail(), password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    startActivityForResult(new Intent(this, EditProfile.class), 1);
                                } else {
                                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        binding.bottomNavigationView.setSelectedItemId(R.id.profile);
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home || itemId == R.id.blog) {
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return false;
            }
            return itemId == R.id.profile;
        });

        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserData();
        }
    }

    private void loadUserData() {
        FirebaseUser user = firebaseManager.getCurrentUser();
        if (user != null) {
            binding.emailInput.setText(user.getEmail());
            firebaseManager.loadUserData(user, this::populateFields, this);
        }
    }

    private void populateFields(User user) {
        binding.fullnameInput.setText(user.getFullName() != null ? user.getFullName() : "Not set");
        binding.username1Input.setText(user.getUsername() != null ? user.getUsername() : "Not set");
        binding.dobInput.setText(user.getDob() != null ? user.getDob() : "Not set");
        binding.genderInput.setText(user.getGender() != null ? user.getGender() : "Not set");
        binding.heightInput.setText(user.getHeight() > 0 ? user.getHeight() + " cm" : "Not set");
        binding.weightInput.setText(user.getWeight() > 0 ? user.getWeight() + " kg" : "Not set");
    }
}