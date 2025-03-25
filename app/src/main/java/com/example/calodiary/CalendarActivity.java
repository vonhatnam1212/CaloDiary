package com.example.calodiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import com.example.calodiary.model.UserHealth;
import com.example.calodiary.model.Meal;

public class CalendarActivity extends AppCompatActivity {
    private TextView tvCurrentWeek;
    private TextView tvTargetCalories;
    private LinearLayout weekContainer;
    private TextView tvSelectedDate;
    private TextView tvDayCalories;
    private ListView listDayMeals;
    
    private Calendar currentDate;
    private double targetCalories;
    private SharedPreferences sharedPreferences;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dayFormat;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserHealth userHealth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        loadUserHealth();
    }

    private void initializeViews() {
        tvCurrentWeek = findViewById(R.id.tvCurrentWeek);
        tvTargetCalories = findViewById(R.id.tvTargetCalories);
        weekContainer = findViewById(R.id.weekContainer);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDayCalories = findViewById(R.id.tvDayCalories);
        listDayMeals = findViewById(R.id.listDayMeals);

        sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);
        currentDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
    }

    private void loadUserHealth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("user_health")
            .document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                userHealth = documentSnapshot.toObject(UserHealth.class);
                if (userHealth != null) {
                    targetCalories = userHealth.getDailyCalorieNeeds();
                    tvTargetCalories.setText(String.format("Mục tiêu: %.0f kcal/ngày", targetCalories));
                    setupWeekView();
                }
            });
    }

    private void setupWeekView() {
        weekContainer.removeAllViews();
        Calendar calendar = (Calendar) currentDate.clone();
        
        // Di chuyển về đầu tuần (thứ 2)
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1);
        }

        // Hiển thị tiêu đề tuần
        String weekTitle = String.format("Tuần: %s - %s",
            dateFormat.format(calendar.getTime()),
            dateFormat.format(getEndOfWeek(calendar)));
        tvCurrentWeek.setText(weekTitle);

        // Tạo view cho 7 ngày trong tuần
        for (int i = 0; i < 7; i++) {
            addDayView(calendar);
            calendar.add(Calendar.DATE, 1);
        }
    }

    private void addDayView(Calendar date) {
        View dayView = LayoutInflater.from(this).inflate(R.layout.item_day, weekContainer, false);
        
        TextView tvDayName = dayView.findViewById(R.id.tvDayName);
        TextView tvDate = dayView.findViewById(R.id.tvDate);
        TextView tvDayCalories = dayView.findViewById(R.id.tvDayCalories);

        // Set data
        tvDayName.setText(dayFormat.format(date.getTime()));
        tvDate.setText(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        
        // Load calories for this day
        double dayCalories = getDayCalories(dateFormat.format(date.getTime()));
        tvDayCalories.setText(String.format("%.0f kcal", dayCalories));
        
        // Set color based on calories
        if (dayCalories < targetCalories) {
            tvDayCalories.setTextColor(Color.RED);
        } else {
            tvDayCalories.setTextColor(Color.GREEN);
        }

        // Highlight today
        if (isToday(date)) {
            dayView.setBackgroundResource(R.drawable.bg_selected_day);
        }

        // Click listener
        final String dateStr = dateFormat.format(date.getTime());
        dayView.setOnClickListener(v -> showDayDetails(dateStr));

        weekContainer.addView(dayView);
    }

    private void showDayDetails(String dateStr) {
        tvSelectedDate.setText(dateStr);
        
        double totalCalories = getDayCalories(dateStr);
        String status = totalCalories < targetCalories ? "Thiếu" : "Đủ";
        tvDayCalories.setText(String.format("Tổng calo: %.0f kcal (%s)", totalCalories, status));
        tvDayCalories.setTextColor(totalCalories < targetCalories ? Color.RED : Color.GREEN);

        // Load and display meals
        List<Meal> dayMeals = getDayMeals(dateStr);
        MealAdapter adapter = new MealAdapter(this, dayMeals);
        listDayMeals.setAdapter(adapter);
    }

    private double getDayCalories(String dateStr) {
        try {
            String mealsJson = sharedPreferences.getString("meals_" + dateStr, null);
            if (mealsJson != null) {
                JSONArray jsonArray = new JSONArray(mealsJson);
                double total = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject meal = jsonArray.getJSONObject(i);
                    total += meal.getDouble("calories");
                }
                return total;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private List<Meal> getDayMeals(String dateStr) {
        List<Meal> meals = new ArrayList<>();
        try {
            String mealsJson = sharedPreferences.getString("meals_" + dateStr, null);
            if (mealsJson != null) {
                JSONArray jsonArray = new JSONArray(mealsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonMeal = jsonArray.getJSONObject(i);
                    meals.add(new Meal(
                        jsonMeal.getInt("type"),
                        jsonMeal.getString("name"),
                        jsonMeal.getDouble("calories"),
                        jsonMeal.getDouble("portion")
                    ));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return meals;
    }

    private Date getEndOfWeek(Calendar calendar) {
        Calendar end = (Calendar) calendar.clone();
        end.add(Calendar.DATE, 6);
        return end.getTime();
    }

    private boolean isToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            && date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
            && date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    // Thêm inner class MealAdapter
    private class MealAdapter extends ArrayAdapter<Meal> {
        private final String[] MEAL_TYPES = {"Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ"};

        MealAdapter(Context context, List<Meal> meals) {
            super(context, 0, meals);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_meal, parent, false);
            }

            TextView tvMealType = convertView.findViewById(R.id.tvMealType);
            TextView tvMealName = convertView.findViewById(R.id.tvMealName);
            TextView tvMealDetails = convertView.findViewById(R.id.tvMealDetails);

            Meal meal = getItem(position);
            tvMealType.setText(MEAL_TYPES[meal.getType()]);
            tvMealName.setText(meal.getName());
            tvMealDetails.setText(String.format(Locale.getDefault(),
                "%.0f calories, %.1f portion",
                meal.getCalories(), meal.getPortion()));

            return convertView;
        }
    }
} 