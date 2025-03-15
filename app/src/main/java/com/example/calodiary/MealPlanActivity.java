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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity{
    private TextView tvDailyCalories;
    private TextView tvRemainingCalories;
    private ListView listViewMeals;
    private Button btnAddMeal;
    
    private double dailyCalories;
    private double remainingCalories;
    private List<Meal> meals;
    private MealAdapter mealAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        initializeViews();
        loadUserData();
        setupListView();
        setupClickListeners();
    }

    private void initializeViews() {
        tvDailyCalories = findViewById(R.id.tvDailyCalories);
        tvRemainingCalories = findViewById(R.id.tvRemainingCalories);
        listViewMeals = findViewById(R.id.listViewMeals);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        
        sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        dailyCalories = sharedPreferences.getFloat("dailyCalories", 0);
        remainingCalories = dailyCalories;
        
        meals = new ArrayList<>();
        String mealsJson = sharedPreferences.getString("meals", null);
        if (mealsJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(mealsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    meals.add(new Meal(
                        jsonObject.getInt("type"),
                        jsonObject.getString("name"),
                        jsonObject.getDouble("calories"),
                        jsonObject.getDouble("portion")
                    ));
                }
                updateRemainingCalories();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        updateCaloriesDisplay();
    }

    private void setupListView() {
        mealAdapter = new MealAdapter(meals);
        listViewMeals.setAdapter(mealAdapter);
        
        // Long click to delete
        listViewMeals.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void setupClickListeners() {
        btnAddMeal.setOnClickListener(v -> showAddMealDialog());
    }

    private void showAddMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        
        Spinner spinnerMealType = dialogView.findViewById(R.id.spinnerMealType);
        EditText etMealName = dialogView.findViewById(R.id.etMealName);
        EditText etCalories = dialogView.findViewById(R.id.etCalories);
        EditText etPortion = dialogView.findViewById(R.id.etPortion);

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"Sáng", "Trưa", "Tối", "Bữa phụ"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);

        builder.setTitle("Thêm món ăn")
               .setView(dialogView)
               .setPositiveButton("Thêm", (dialog, which) -> {
                   if (validateMealInput(etMealName, etCalories, etPortion)) {
                       addMeal(
                           spinnerMealType.getSelectedItemPosition(),
                           etMealName.getText().toString(),
                           Double.parseDouble(etCalories.getText().toString()),
                           Double.parseDouble(etPortion.getText().toString())
                       );
                   }
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa món ăn")
            .setMessage("Bạn có chắc muốn xóa món ăn này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                meals.remove(position);
                saveMeals();
                mealAdapter.notifyDataSetChanged();
                updateRemainingCalories();
                updateCaloriesDisplay();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private boolean validateMealInput(@NonNull EditText etMealName, @NonNull EditText etCalories, @NonNull EditText etPortion) {
        String name = etMealName.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();
        String portion = etPortion.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (calories.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập lượng calo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (portion.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập khẩu phần", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addMeal(int type, String name, double calories, double portion) {
        Meal meal = new Meal(type, name, calories, portion);
        meals.add(meal);
        mealAdapter.notifyDataSetChanged();
        saveMeals();
        updateRemainingCalories();
        updateCaloriesDisplay();
        Toast.makeText(this, "Đã thêm món ăn", Toast.LENGTH_SHORT).show();
    }

    private void saveMeals() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Meal meal : meals) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", meal.type);
                jsonObject.put("name", meal.name);
                jsonObject.put("calories", meal.calories);
                jsonObject.put("portion", meal.portion);
                jsonArray.put(jsonObject);
            }
            sharedPreferences.edit()
                .putString("meals", jsonArray.toString())
                .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateRemainingCalories() {
        remainingCalories = dailyCalories;
        for (Meal meal : meals) {
            remainingCalories -= meal.calories;
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateCaloriesDisplay() {
        tvDailyCalories.setText(String.format("Lượng calo cần thiết: %.0f kcal", dailyCalories));
        tvRemainingCalories.setText(String.format("Còn lại: %.0f kcal", remainingCalories));
    }

    private class MealAdapter extends ArrayAdapter<Meal> {
        MealAdapter(List<Meal> meals) {
            super(MealPlanActivity.this, R.layout.item_meal, meals);
        }

        @NonNull
        @SuppressLint("DefaultLocale")
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_meal, parent, false);
            }

            Meal meal = getItem(position);
            if (meal != null) {
                TextView tvMealType = convertView.findViewById(R.id.tvMealType);
                TextView tvMealName = convertView.findViewById(R.id.tvMealName);
                TextView tvMealCalories = convertView.findViewById(R.id.tvMealCalories);

                String[] mealTypes = {"Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ"};
                tvMealType.setText(mealTypes[meal.type]);
                tvMealName.setText(meal.name);
                tvMealCalories.setText(String.format("%.0f kcal (%.0fg)", 
                    meal.calories, meal.portion));
            }

            return convertView;
        }
    }

    public static class Meal {
        int type;
        String name;
        double calories;
        double portion;

        Meal(int type, String name, double calories, double portion) {
            this.type = type;
            this.name = name;
            this.calories = calories;
            this.portion = portion;
        }
    }
}

