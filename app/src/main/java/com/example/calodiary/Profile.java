package com.example.calodiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calodiary.chat.ChatHistoryActivity;
import com.example.calodiary.databinding.ActivityProfileBinding;
import com.example.calodiary.Model.User;
import com.example.calodiary.utils.FirebaseManager;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseManager firebaseManager;
    
    // Custom bottom navigation views
    private LinearLayout tabHome, tabBlog, tabChat, tabProfile;
    private ImageView iconHome, iconBlog, iconChat, iconProfile;

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

        setupCustomBottomNavigation();
        loadUserData();
    }

    private void setupCustomBottomNavigation() {
        // Initialize bottom navigation
        View bottomNav = findViewById(R.id.bottomNavigation);
        tabHome = bottomNav.findViewById(R.id.tab_home);
        tabBlog = bottomNav.findViewById(R.id.tab_blog);
        tabChat = bottomNav.findViewById(R.id.tab_chat);
        tabProfile = bottomNav.findViewById(R.id.tab_profile);
        
        iconHome = bottomNav.findViewById(R.id.icon_home);
        iconBlog = bottomNav.findViewById(R.id.icon_blog);
        iconChat = bottomNav.findViewById(R.id.icon_chat);
        iconProfile = bottomNav.findViewById(R.id.icon_profile);
        
        // Set profile tab as selected
        setSelectedTab(tabProfile, iconProfile);
        
        // Set click listeners for each tab
        tabHome.setOnClickListener(v -> {
            setSelectedTab(tabHome, iconHome);
            navigateToActivity(HomeActivity.class);
        });
        
        tabBlog.setOnClickListener(v -> {
            setSelectedTab(tabBlog, iconBlog);
            navigateToActivity(ArticleActivity.class);
        });
        
        tabChat.setOnClickListener(v -> {
            setSelectedTab(tabChat, iconChat);
            navigateToActivity(ChatHistoryActivity.class);
        });
        
        tabProfile.setOnClickListener(v -> {
            if (!isCurrentActivity(Profile.class)) {
                setSelectedTab(tabProfile, iconProfile);
                // Already in Profile
            }
        });
    }
    
    private void setSelectedTab(LinearLayout tab, ImageView icon) {
        // Reset all tabs to unselected state
        tabHome.setSelected(false);
        tabBlog.setSelected(false);
        tabChat.setSelected(false);
        tabProfile.setSelected(false);
        
        iconHome.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconBlog.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconChat.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconProfile.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        
        // Set selected tab
        tab.setSelected(true);
        icon.setColorFilter(getResources().getColor(R.color.primary));
    }
    
    private boolean isCurrentActivity(Class<?> activityClass) {
        return getClass().getName().equals(activityClass.getName());
    }
    
    private void navigateToActivity(Class<?> destinationActivity) {
        try {
            Intent intent = new Intent(this, destinationActivity);
            startActivity(intent);
            finish(); // Finish current activity to avoid stacking
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở màn hình này", Toast.LENGTH_SHORT).show();
        }
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