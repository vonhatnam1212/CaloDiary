package com.example.calodiary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.widget.SearchView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.appbar.MaterialToolbar;
import android.content.Intent;
import com.google.android.material.textfield.TextInputEditText;
import com.example.calodiary.adapters.MealAdapter;
import com.example.calodiary.models.Meal;
import com.google.android.material.chip.ChipGroup;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import android.widget.ProgressBar;
import com.example.calodiary.adapters.MealHistoryAdapter;
import java.util.Collections;

public class MealPlanActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;
    private RecyclerView rvMeals;
    private MealAdapter mealAdapter;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupMealTypes;
    private List<Meal> allMeals = new ArrayList<>();
    private ExtendedFloatingActionButton fabAddMeal;
    private double dailyCalorieGoal = 2000; // Default value
    private RecyclerView rvMealHistory;
    private MealHistoryAdapter mealHistoryAdapter;
    private TextView tvCalorieProgress;
    private ProgressBar progressCalories;
    private MaterialButton btnSelectDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initializeViews();
        setupRecyclerView();
        loadMeals();
        setupSearchAndFilters();
        loadUserCalorieGoal();
        setupAddMealButton();
    }

    private void initializeViews() {
        rvMeals = findViewById(R.id.rvMeals);
        etSearch = findViewById(R.id.etSearch);
        chipGroupMealTypes = findViewById(R.id.chipGroupMealTypes);
        
        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvMealHistory = findViewById(R.id.rvMealHistory);
        tvCalorieProgress = findViewById(R.id.tvCalorieProgress);
        progressCalories = findViewById(R.id.progressCalories);
        btnSelectDate = findViewById(R.id.btnSelectDate);

        // Setup RecyclerView
        rvMealHistory.setLayoutManager(new LinearLayoutManager(this));
        mealHistoryAdapter = new MealHistoryAdapter();
        rvMealHistory.setAdapter(mealHistoryAdapter);

        // Setup date selection
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void setupRecyclerView() {
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter();
        rvMeals.setAdapter(mealAdapter);
    }

    private void setupSearchAndFilters() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMeals(s.toString(), getSelectedMealType());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupMealTypes.setOnCheckedChangeListener((group, checkedId) -> {
            String mealType = "";
            if (checkedId == R.id.chipBreakfast) mealType = "Breakfast";
            else if (checkedId == R.id.chipLunch) mealType = "Lunch";
            else if (checkedId == R.id.chipDinner) mealType = "Dinner";
            else if (checkedId == R.id.chipSnacks) mealType = "Snacks";
            
            filterMeals(etSearch.getText().toString(), mealType);
        });
    }

    private String getSelectedMealType() {
        int checkedId = chipGroupMealTypes.getCheckedChipId();
        if (checkedId == R.id.chipBreakfast) return "Breakfast";
        if (checkedId == R.id.chipLunch) return "Lunch";
        if (checkedId == R.id.chipDinner) return "Dinner";
        if (checkedId == R.id.chipSnacks) return "Snacks";
        return "";
    }

    private void filterMeals(String query, String mealType) {
        List<Meal> filteredMeals = new ArrayList<>();
        
        for (Meal meal : allMeals) {
            boolean matchesQuery = query.isEmpty() || 
                meal.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesType = mealType.isEmpty() || 
                meal.getType().equals(mealType);
            
            if (matchesQuery && matchesType) {
                filteredMeals.add(meal);
            }
        }
        
        mealAdapter.setMeals(filteredMeals);
    }

    private void loadMeals() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date());

        showLoading(true);

        db.collection("meals")
            .whereEqualTo("userId", userId)
//            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocuments -> {
                List<Meal> meals = new ArrayList<>();
                int todayCalories = 0;
                
                for (DocumentSnapshot doc : queryDocuments) {
                    Meal meal = doc.toObject(Meal.class);
                    if (meal != null) {
                        meal.setId(doc.getId());
                        meals.add(meal);
                        
                        if (today.equals(meal.getDate())) {
                            todayCalories += meal.getCalories();
                        }
                    }
                }
                Collections.sort(meals, (m1, m2) ->
                        Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                mealAdapter.setMeals(meals);
                updateCalorieProgress(todayCalories);
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                Log.e("MealPlanActivity", "Error loading meals", e);
                Toast.makeText(this, "Error loading meals: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                showLoading(false);
            });
    }

    private void loadUserCalorieGoal() {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        // Assuming you have a method to calculate daily calories
                        dailyCalorieGoal = calculateDailyCalories(user);
                    }
                }
            });
    }

    private void setupAddMealButton() {
        fabAddMeal = findViewById(R.id.fabAddMeal);
        fabAddMeal.setOnClickListener(v -> showAddMealDialog());
    }

    private void showAddMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        builder.setView(dialogView);

        TextInputEditText etMealName = dialogView.findViewById(R.id.etMealName);
        TextInputEditText etCalories = dialogView.findViewById(R.id.etCalories);
        Spinner spinnerMealType = dialogView.findViewById(R.id.spinnerMealType);
        
        // Setup meal type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.meal_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);

        builder.setTitle("Add New Meal")
            .setPositiveButton("Save", null) // We'll set this later
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (validateMealInput(etMealName, etCalories)) {
                    saveMeal(
                        etMealName.getText().toString(),
                        Integer.parseInt(etCalories.getText().toString()),
                        spinnerMealType.getSelectedItem().toString(),
                        dialog
                    );
                }
            });
        });

        dialog.show();
    }

    private boolean validateMealInput(TextInputEditText etMealName, TextInputEditText etCalories) {
        String name = etMealName.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();

        if (name.isEmpty()) {
            etMealName.setError("Please enter meal name");
            return false;
        }

        if (calories.isEmpty()) {
            etCalories.setError("Please enter calories");
            return false;
        }

        try {
            int caloriesValue = Integer.parseInt(calories);
            if (caloriesValue <= 0 || caloriesValue > 5000) {
                etCalories.setError("Please enter a valid calorie amount (1-5000)");
                return false;
            }
        } catch (NumberFormatException e) {
            etCalories.setError("Please enter a valid number");
            return false;
        }

        return true;
    }

    private void saveMeal(String name, int calories, String type, AlertDialog dialog) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date());

        Map<String, Object> mealData = new HashMap<>();
        mealData.put("name", name);
        mealData.put("type", type);
        mealData.put("calories", calories);
        mealData.put("userId", userId);
        mealData.put("date", date);
        mealData.put("timestamp", new Date().getTime());

        FirebaseFirestore.getInstance().collection("meals")
            .add(mealData)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadMeals(); // Refresh the list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error adding meal: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private double calculateDailyCalories(User user) {
        if (user == null) return 0.0;

        try {
            double weight = user.getWeight();
            double height = user.getHeight();
            int age = user.getAge();
            double activityLevel = 1.2; // default value
            
            try {
                String activityLevelStr = user.getActivityLevel();
                if (activityLevelStr != null && !activityLevelStr.trim().isEmpty()) {
                    activityLevel = Double.parseDouble(activityLevelStr);
                }
            } catch (NumberFormatException e) {
                Log.e("MealPlanActivity", "Error parsing activity level: " + e.getMessage());
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
            Log.e("MealPlanActivity", "Error calculating calories: " + e.getMessage());
            return 0.0;
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(selection));
            loadMealsForDate(selectedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void loadMealsForDate(String date) {
        showLoading(true);
        
        db.collection("meals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("date", date)
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
                
                // Sắp xếp locally
                Collections.sort(meals, (m1, m2) -> 
                    Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                
                mealAdapter.setMeals(meals);
                updateCalorieProgress(totalCalories);
                showLoading(false);

                // Log để debug
                Log.d("MealPlanActivity", "Loaded " + meals.size() + 
                    " meals for date " + date);
            })
            .addOnFailureListener(e -> {
                Log.e("MealPlanActivity", "Error loading meals for date " + date, e);
                Toast.makeText(this, "Error loading meals: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                showLoading(false);
            });
    }

    private void updateCalorieProgress(int totalCalories) {
        int progress = (int)((totalCalories * 100.0) / dailyCalorieGoal);
        progressCalories.setProgress(Math.min(progress, 100));

        tvCalorieProgress.setText(String.format(Locale.getDefault(),
            "%d/%d cal (%d%%)", totalCalories, (int)dailyCalorieGoal, progress));

        tvCalorieProgress.setTextColor(getResources().getColor(
            totalCalories >= dailyCalorieGoal ? R.color.goal_met_text : R.color.goal_not_met_text));
    }

    private void showLoading(boolean show) {
        if (show) {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}

