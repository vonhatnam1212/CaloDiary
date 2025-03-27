package com.example.calodiary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
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
    private BottomNavigationView bottomNav;

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
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvCalorieGoal = findViewById(R.id.tvCalorieGoal);
        rvRecentMeals = findViewById(R.id.rvRecentMeals);
        bottomNav = findViewById(R.id.bottomNavigation);
        
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
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocuments -> {
                List<Meal> meals = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocuments) {
                    Meal meal = doc.toObject(Meal.class);
                    if (meal != null) {
                        meals.add(meal);
                    }
                }
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

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                return true;
            } else if (itemId == R.id.blog) {
                startActivity(new Intent(this, PostAdapter.class));
                return true;
            } else if (itemId == R.id.AIchat) {
                return true;
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            }
            return false;
        });
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