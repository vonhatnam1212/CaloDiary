package com.example.calodiary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.CalendarView;
import com.example.calodiary.adapters.DayMealsAdapter;
import com.example.calodiary.models.Meal;
import java.util.concurrent.atomic.AtomicInteger;
import android.graphics.drawable.GradientDrawable;
import com.example.calodiary.models.DayData;
import com.example.calodiary.adapters.WeekDaysAdapter;
import com.google.firebase.firestore.Query;
import android.widget.Toast;

public class CalendarActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;
    private RecyclerView rvWeekDays;
    private RecyclerView rvDayMeals;
    private TextView tvSelectedDate;
    private TextView tvTotalCalories;
    private WeekDaysAdapter weekDaysAdapter;
    private DayMealsAdapter mealsAdapter;
    private double dailyCalorieGoal;
    private Calendar currentWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentWeek = Calendar.getInstance();

        initializeViews();
        loadUserCalorieGoal();
        loadCurrentWeek();
    }

    private void initializeViews() {
        rvWeekDays = findViewById(R.id.rvWeekDays);
        rvDayMeals = findViewById(R.id.rvDayMeals);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);

        // Setup RecyclerViews
        rvWeekDays.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        weekDaysAdapter = new WeekDaysAdapter(new ArrayList<>(), this::onDaySelected);
        rvWeekDays.setAdapter(weekDaysAdapter);

        rvDayMeals.setLayoutManager(new LinearLayoutManager(this));
        mealsAdapter = new DayMealsAdapter(new ArrayList<>());
        rvDayMeals.setAdapter(mealsAdapter);

        // Setup navigation buttons
        findViewById(R.id.btnPreviousWeek).setOnClickListener(v -> navigateWeek(-1));
        findViewById(R.id.btnNextWeek).setOnClickListener(v -> navigateWeek(1));
    }

    private void loadUserCalorieGoal() {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        dailyCalorieGoal = calculateDailyCalories(user);
                        loadCurrentWeek(); // Reload after getting calorie goal
                    }
                }
            });
    }

    private void navigateWeek(int weekOffset) {
        currentWeek.add(Calendar.WEEK_OF_YEAR, weekOffset);
        loadCurrentWeek();
    }

    private void loadCurrentWeek() {
        List<DayData> weekDays = new ArrayList<>();
        Calendar calendar = (Calendar) currentWeek.clone();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        // Update week header
        String weekHeader = String.format(Locale.getDefault(), "Week of %s",
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(calendar.getTime()));
        tvSelectedDate.setText(weekHeader);

        // Load each day of the week
        for (int i = 0; i < 7; i++) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());
            
            DayData dayData = new DayData(date, calendar.getTime(), dailyCalorieGoal);
            weekDays.add(dayData);
            
            // Load meals for each day
            loadDayCalories(dayData);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        weekDaysAdapter.updateDays(weekDays);
    }

    private void loadDayCalories(DayData dayData) {
        db.collection("meals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", dayData.getDate())
            .get()
            .addOnSuccessListener(queryDocuments -> {
                int totalCalories = 0;
                List<Meal> dayMeals = new ArrayList<>();
                
                for (DocumentSnapshot doc : queryDocuments) {
                    Meal meal = doc.toObject(Meal.class);
                    if (meal != null) {
                        meal.setId(doc.getId());
                        dayMeals.add(meal);
                        totalCalories += meal.getCalories();
                    }
                }
                
                dayData.setMeals(dayMeals);
                dayData.setCalories(totalCalories);
                weekDaysAdapter.notifyDataSetChanged();

                // Log để debug
                Log.d("CalendarActivity", "Loaded " + dayMeals.size() + 
                    " meals for " + dayData.getDate() + 
                    ", total calories: " + totalCalories);
            })
            .addOnFailureListener(e -> {
                Log.e("CalendarActivity", "Error loading meals for " + 
                    dayData.getDate(), e);
            });
    }

    private void onDaySelected(DayData dayData) {
        tvSelectedDate.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            .format(dayData.getDateTime()));
        loadDayMeals(dayData.getDate());
    }

    private void loadDayMeals(String date) {
        showLoading(true);

        db.collection("meals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
//            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocuments -> {
                List<Meal> meals = new ArrayList<>();
                int totalCalories = 0;
                
                for (DocumentSnapshot doc : queryDocuments) {
                    Meal meal = doc.toObject(Meal.class);
                    if (meal != null) {
                        meal.setId(doc.getId());
                        meals.add(meal);
                        totalCalories += meal.getCalories();
                    }
                }
                Collections.sort(meals, (m1, m2) ->
                        Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                // Cập nhật UI
                mealsAdapter.setMeals(meals);
                updateDayCalories(totalCalories);
                showLoading(false);

                // Log để debug
                Log.d("CalendarActivity", "Loaded " + meals.size() + 
                    " meals for date " + date);
            })
            .addOnFailureListener(e -> {
                Log.e("CalendarActivity", "Error loading meals for date " + date, e);
                Toast.makeText(this, "Error loading meals: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                showLoading(false);
            });
    }

    private void updateDayCalories(int totalCalories) {
        String status = totalCalories >= dailyCalorieGoal ? "Goal reached!" : "Under goal";
        int colorRes = totalCalories >= dailyCalorieGoal ? 
            R.color.green : R.color.red;

        tvTotalCalories.setText(String.format(Locale.getDefault(),
            "Total Calories: %d / %.0f (%s)",
            totalCalories, dailyCalorieGoal, status));
        tvTotalCalories.setTextColor(getResources().getColor(colorRes));
    }

    private double calculateDailyCalories(User userData) {
        if (userData == null) return 0;

        try {
            double weight = userData.getWeight();
            double height = userData.getHeight();
            int age = userData.getAge();
            double activityLevel = userData.getActivityLevelValue();
            String gender = userData.getGender();

            // Calculate BMR using Mifflin-St Jeor Equation
            double bmr;
            if ("Male".equals(gender)) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }

            return bmr * activityLevel;
        } catch (Exception e) {
            Log.e("CalendarActivity", "Error calculating calories: " + e.getMessage());
            return 0;
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
} 