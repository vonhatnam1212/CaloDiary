package com.example.calodiary;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity {
    private TextView tvDailyCalories;
    private TextView tvRemainingCalories;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button btnAddMeal;
    
    private double dailyCalories;
    private double remainingCalories;
    private List<Meal> meals;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        initializeViews();
        loadUserData();
        setupTabs();
        setupClickListeners();
    }

    private void initializeViews() {
        tvDailyCalories = findViewById(R.id.tvDailyCalories);
        tvRemainingCalories = findViewById(R.id.tvRemainingCalories);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        
        sharedPreferences = getSharedPreferences("CaloDiaryPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        dailyCalories = sharedPreferences.getFloat("dailyCalories", 0);
        remainingCalories = dailyCalories;
        
        // Load saved meals
        String mealsJson = sharedPreferences.getString("meals", null);
        meals = new ArrayList<>();
        
        if (mealsJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(mealsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Meal meal = new Meal(
                        jsonObject.getInt("type"),
                        jsonObject.getString("name"),
                        jsonObject.getDouble("calories"),
                        jsonObject.getDouble("portion")
                    );
                    meals.add(meal);
                }
                updateRemainingCalories();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateCaloriesDisplay();
    }

    private void setupTabs() {
        // Tạo adapter cho ViewPager2
        MealPagerAdapter pagerAdapter = new MealPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Thêm các tab với contentDescription
        String[] tabTitles = {"Sáng", "Trưa", "Tối", "Bữa phụ"};
        String[] tabDescriptions = {
            "Danh sách món ăn buổi sáng",
            "Danh sách món ăn buổi trưa",
            "Danh sách món ăn buổi tối",
            "Danh sách món ăn phụ"
        };

        for (int i = 0; i < tabTitles.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(tabTitles[i]);
            tab.setContentDescription(tabDescriptions[i]);
            tabLayout.addTab(tab);
        }

        // Liên kết TabLayout với ViewPager2
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void setupClickListeners() {
        btnAddMeal.setOnClickListener(v -> showAddMealDialog());
    }

    private void showAddMealDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_meal);

        Spinner spinnerMealType = dialog.findViewById(R.id.spinnerMealType);
        EditText etMealName = dialog.findViewById(R.id.etMealName);
        EditText etCalories = dialog.findViewById(R.id.etCalories);
        EditText etPortion = dialog.findViewById(R.id.etPortion);
        Button btnAdd = dialog.findViewById(R.id.btnAddMeal);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"Sáng", "Trưa", "Tối", "Bữa phụ"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            if (validateMealInput(etMealName, etCalories, etPortion)) {
                addMeal(
                    spinnerMealType.getSelectedItemPosition(),
                    etMealName.getText().toString(),
                    Double.parseDouble(etCalories.getText().toString()),
                    Double.parseDouble(etPortion.getText().toString())
                );
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validateMealInput(EditText etMealName, EditText etCalories, EditText etPortion) {
        if (etMealName.getText().toString().trim().isEmpty()) {
            etMealName.setError("Vui lòng nhập tên món ăn");
            return false;
        }
        if (etCalories.getText().toString().trim().isEmpty()) {
            etCalories.setError("Vui lòng nhập lượng calo");
            return false;
        }
        if (etPortion.getText().toString().trim().isEmpty()) {
            etPortion.setError("Vui lòng nhập khẩu phần");
            return false;
        }
        return true;
    }

    private void addMeal(int type, String name, double calories, double portion) {
        Meal meal = new Meal(type, name, calories, portion);
        meals.add(meal);
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
            sharedPreferences.edit().putString("meals", jsonArray.toString()).apply();
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

    private void updateCaloriesDisplay() {
        tvDailyCalories.setText(String.format("Lượng calo cần thiết: %.0f kcal", dailyCalories));
        tvRemainingCalories.setText(String.format("Còn lại: %.0f kcal", remainingCalories));
    }

    static class Meal {
        private int type; // 0: Sáng, 1: Trưa, 2: Tối, 3: Bữa phụ
        private String name;
        private double calories;
        private double portion;

        public Meal(int type, String name, double calories, double portion) {
            this.type = type;
            this.name = name;
            this.calories = calories;
            this.portion = portion;
        }

        public double getCalories() {
            return calories;
        }

        // Các getters và setters khác nếu cần
    }
}

// Thêm class MealPagerAdapter
class MealPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 4;

    public MealPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Tạo fragment cho mỗi tab
        return MealListFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}

