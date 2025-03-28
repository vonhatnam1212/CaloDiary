package com.example.calodiary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calodiary.chat.ChatHistoryActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.example.calodiary.adapters.RecentMealsAdapter;
import com.example.calodiary.models.Meal;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private TextView tvWelcome, tvCalorieGoal;
    private RecyclerView rvRecentMeals;
    private RecentMealsAdapter mealsAdapter;
    
    // Custom bottom navigation views
    private LinearLayout tabHome, tabBlog, tabChat, tabProfile;
    private ImageView iconHome, iconBlog, iconChat, iconProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        initializeViews();
        loadUserData();
        loadRecentMeals();
        setupNavigationCards();
        setupCustomBottomNavigation();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvCalorieGoal = findViewById(R.id.tvCalorieGoal);
        rvRecentMeals = findViewById(R.id.rvRecentMeals);
        
        // Initialize custom bottom navigation
        View bottomNav = findViewById(R.id.bottomNavigation);
        tabHome = bottomNav.findViewById(R.id.tab_home);
        tabBlog = bottomNav.findViewById(R.id.tab_blog);
        tabChat = bottomNav.findViewById(R.id.tab_chat);
        tabProfile = bottomNav.findViewById(R.id.tab_profile);
        
        iconHome = bottomNav.findViewById(R.id.icon_home);
        iconBlog = bottomNav.findViewById(R.id.icon_blog);
        iconChat = bottomNav.findViewById(R.id.icon_chat);
        iconProfile = bottomNav.findViewById(R.id.icon_profile);
        
        mealsAdapter = new RecentMealsAdapter();
        rvRecentMeals.setLayoutManager(new LinearLayoutManager(this));
        rvRecentMeals.setAdapter(mealsAdapter);
    }

    private void loadUserData() {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String name = document.getString("name");
                    if (name != null) {
                        tvWelcome.setText("Welcome, " + name);
                    }

                    // Tạo đối tượng User từ document
                    User user = new User();
                    user.setWeight(getDoubleValue(document, "weight", 0.0));
                    user.setHeight(getDoubleValue(document, "height", 0.0));
                    user.setAge(getIntValue(document, "age", 25));
                    user.setGender(getStringValue(document, "gender", "male"));
                    user.setActivityLevel(getStringValue(document, "activityLevel", "1.2"));

                    updateCalorieGoal(user);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            });
    }

    // Helper methods to safely get values from DocumentSnapshot
    private double getDoubleValue(DocumentSnapshot document, String field, double defaultValue) {
        Object value = document.get(field);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private int getIntValue(DocumentSnapshot document, String field, int defaultValue) {
        Object value = document.get(field);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String getStringValue(DocumentSnapshot document, String field, String defaultValue) {
        String value = document.getString(field);
        return value != null ? value : defaultValue;
    }

    private void loadRecentMeals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date());
            
        db.collection("meals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", today)
//            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(queryDocuments -> {
                List<Meal> meals = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocuments) {
                    Meal meal = doc.toObject(Meal.class);
                    if (meal != null) {
                        meals.add(meal);
                    }
                }
                Collections.sort(meals, (m1, m2) ->
                        Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                mealsAdapter.setMeals(meals);
            });
    }

    private double calculateDailyCalories(User user) {
        if (user == null) return 0.0;

        try {
            double weight = user.getWeight();
            double height = user.getHeight();
            int age = user.getAge();
            // Đặt giá trị mặc định cho activityLevel nếu null hoặc không hợp lệ
            double activityLevel = 1.2; // giá trị mặc định
            try {
                String activityLevelStr = user.getActivityLevel();
                if (activityLevelStr != null && !activityLevelStr.trim().isEmpty()) {
                    activityLevel = Double.parseDouble(activityLevelStr);
                }
            } catch (NumberFormatException e) {
                Log.e("HomeActivity", "Error parsing activity level: " + e.getMessage());
            }
            
            // Calculate BMR using the Mifflin-St Jeor Equation
            double bmr;
            if ("male".equalsIgnoreCase(user.getGender())) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }
            
            return bmr * activityLevel;
        } catch (Exception e) {
            Log.e("HomeActivity", "Error calculating calories: " + e.getMessage());
            return 0.0;
        }
    }

    private void updateCalorieGoal(User user) {
        double dailyCalories = calculateDailyCalories(user);
        tvCalorieGoal.setText(String.format(Locale.getDefault(), 
            "Daily Goal: %.0f calories", dailyCalories));
    }

    private void setupCustomBottomNavigation() {
        // Set home tab as initially selected
        setSelectedTab(tabHome, iconHome);
        
        // Set click listeners for each tab
        tabHome.setOnClickListener(v -> {
            if (!isCurrentActivity(HomeActivity.class)) {
                setSelectedTab(tabHome, iconHome);
                // Already in HomeActivity
            }
        });
        
        tabBlog.setOnClickListener(v -> {
            setSelectedTab(tabBlog, iconBlog);
            navigateToActivity(ArticleActivity.class);
        });
        
        tabChat.setOnClickListener(v -> {
            setSelectedTab(tabChat, iconChat);
            // Navigate to Chat - Assume you have a ChatActivity class
            navigateToActivity(ChatHistoryActivity.class);
        });
        
        tabProfile.setOnClickListener(v -> {
            setSelectedTab(tabProfile, iconProfile);
            navigateToActivity(Profile.class);
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

    private void setupNavigationCards() {
        // Body Index navigation
        View cardBodyIndex = findViewById(R.id.cardBodyIndex);
        cardBodyIndex.setOnClickListener(v -> navigateToActivity(BodyIndexActivity.class));

        // Meal Plan navigation  
        View cardMealPlan = findViewById(R.id.cardMealPlan);
        cardMealPlan.setOnClickListener(v -> navigateToActivity(MealPlanActivity.class));

        // Calendar navigation
        View cardCalendar = findViewById(R.id.cardCalendar);
        cardCalendar.setOnClickListener(v -> navigateToActivity(CalendarActivity.class));
    }

    private void navigateToActivity(Class<?> destinationActivity) {
        try {
            Intent intent = new Intent(this, destinationActivity);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở màn hình này", Toast.LENGTH_SHORT).show();
        }
    }
}