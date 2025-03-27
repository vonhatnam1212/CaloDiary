package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private TextView tvCalories;
    private RecyclerView rvRecentMeals;
    private RecentMealsAdapter recentMealsAdapter;
    private List<Meal> recentMeals;
    private FirebaseFirestore db;
    private double dailyCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        tvCalories = findViewById(R.id.tvCalories);
        rvRecentMeals = findViewById(R.id.rvRecentMeals);

        // Set up RecyclerView
        recentMeals = new ArrayList<>();
        recentMealsAdapter = new RecentMealsAdapter(recentMeals);
        rvRecentMeals.setLayoutManager(new LinearLayoutManager(this));
        rvRecentMeals.setAdapter(recentMealsAdapter);

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                // Already on home
                return true;
            } else if (itemId == R.id.blog) {
                // Handle blog navigation if needed
                return true;
            } else if (itemId == R.id.AIchat) {
                // Handle AI chat navigation if needed
                return true;
            } else if (itemId == R.id.profile) {
                startActivity(new Intent(this, Profile.class));
                return true;
            }
            return false;
        });

        // Setup click listeners for cards in home screen
        findViewById(R.id.cardBodyIndex).setOnClickListener(v -> {
            Intent intent = new Intent(this, BodyIndexActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardMealPlan).setOnClickListener(v -> {
            Intent intent = new Intent(this, MealPlanActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardCalendar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
        });

        // Load user data and recent meals
        loadUserData();
        loadRecentMeals();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User userData = documentSnapshot.toObject(User.class);
                    if (userData != null) {
                        tvWelcome.setText(String.format("Welcome, %s!", userData.getName()));
                        dailyCalories = calculateDailyCalories(userData);
                        tvCalories.setText(String.format("Daily Calorie Goal: %d", (int) dailyCalories));
                    }
                });
    }

    private void loadRecentMeals() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("meals")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recentMeals.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = document.getData();
                        String type = (String) data.get("type");
                        String name = (String) data.get("name");
                        int calories = ((Long) data.get("calories")).intValue();
                        recentMeals.add(new Meal(type, name, calories));
                    }
                    recentMealsAdapter.notifyDataSetChanged();
                });
    }

    private double calculateDailyCalories(User userData) {
        // Mifflin-St Jeor Equation
        double bmr;
        if (userData.getGender().equals("Male")) {
            bmr = (10 * userData.getWeight()) + (6.25 * userData.getHeight()) - (5 * userData.getAge()) + 5;
        } else {
            bmr = (10 * userData.getWeight()) + (6.25 * userData.getHeight()) - (5 * userData.getAge()) - 161;
        }
        return bmr * Double.parseDouble(userData.getActivityLevel());
    }
}