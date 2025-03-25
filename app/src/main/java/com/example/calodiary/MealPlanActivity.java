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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import com.example.calodiary.model.DailyMeal;
import com.example.calodiary.model.Meal;

public class MealPlanActivity extends AppCompatActivity{
    private TextView tvDailyCalories;
    private TextView tvRemainingCalories;
    private ListView listViewMeals;
    private Button btnAddMeal;
    
    private double dailyCalories;
    private double remainingCalories;
    private List<Meal> meals = new ArrayList<>();
    private MealAdapter mealAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(new Date());

        initializeViews();
        loadUserData();
        setupListView();
        setupClickListeners();
        loadMealsFromFirebase();
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
        
        String mealsJson = sharedPreferences.getString("meals", null);
        if (mealsJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(mealsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    addMealFromJson(jsonObject);
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
                           etMealName.getText().toString(),
                           spinnerMealType.getSelectedItemPosition(),
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

    private void addMeal(String name, int type, double calories, double portion) {
        Meal meal = new Meal(type, name, calories, portion);
        meals.add(meal);
        updateMealList();
        saveMeals();
        updateRemainingCalories();
        updateCaloriesDisplay();
        Toast.makeText(this, "Đã thêm món ăn", Toast.LENGTH_SHORT).show();
    }

    private void saveMeals() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Meal meal : meals) {
                JSONObject jsonObject = mealToJson(meal);
                jsonArray.put(jsonObject);
            }
            
            // Lưu cho ngày hiện tại
            String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
            sharedPreferences.edit()
                .putString("meals_" + today, jsonArray.toString())
                .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateRemainingCalories() {
        remainingCalories = dailyCalories;
        for (Meal meal : meals) {
            remainingCalories -= meal.getCalories();
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateCaloriesDisplay() {
        tvDailyCalories.setText(String.format("Lượng calo cần thiết: %.0f kcal", dailyCalories));
        tvRemainingCalories.setText(String.format("Còn lại: %.0f kcal", remainingCalories));
    }

    private void loadMealsFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("daily_meals")
            .document(currentUser.getUid() + "_" + currentDate)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                DailyMeal dailyMeal = documentSnapshot.toObject(DailyMeal.class);
                if (dailyMeal != null) {
                    meals = dailyMeal.getMeals();
                    mealAdapter.notifyDataSetChanged();
                    updateRemainingCalories();
                    updateCaloriesDisplay();
                }
            });
    }

    private void saveMealsToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DailyMeal dailyMeal = new DailyMeal(currentUser.getUid(), currentDate, meals);

        db.collection("daily_meals")
            .document(currentUser.getUid() + "_" + currentDate)
            .set(dailyMeal)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(this, "Đã lưu thực đơn", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addMealFromJson(JSONObject jsonObject) throws JSONException {
        Meal meal = new Meal(
            jsonObject.getInt("type"),
            jsonObject.getString("name"),
            jsonObject.getDouble("calories"),
            jsonObject.getDouble("portion")
        );
        meals.add(meal);
    }

    private JSONObject mealToJson(Meal meal) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", meal.getType());
        jsonObject.put("name", meal.getName());
        jsonObject.put("calories", meal.getCalories());
        jsonObject.put("portion", meal.getPortion());
        return jsonObject;
    }

    private void updateMealList() {
        mealAdapter.notifyDataSetChanged();
    }

    private class MealAdapter extends ArrayAdapter<Meal> {
        MealAdapter(List<Meal> meals) {
            super(MealPlanActivity.this, R.layout.item_meal, meals);
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
            String[] mealTypes = {"Bữa sáng", "Bữa trưa", "Bữa tối", "Bữa phụ"};
            tvMealType.setText(mealTypes[meal.getType()]);
            tvMealName.setText(meal.getName());
            tvMealDetails.setText(String.format(Locale.getDefault(),
                "%.0f calories, %.1f portion",
                meal.getCalories(), meal.getPortion()));

            return convertView;
        }
    }
}

