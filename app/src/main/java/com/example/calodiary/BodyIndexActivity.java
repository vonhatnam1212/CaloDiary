package com.example.calodiary;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DecimalFormat;

public class BodyIndexActivity extends AppCompatActivity {
    private static final String TAG = "BodyIndexActivity";
    private EditText etAge, etWeight, etHeight, etActivity;
    private RadioGroup rgGender;
    private TextView tvBMI, tvBMICategory, tvCalories;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_index);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        try {
            etAge = findViewById(R.id.etAge);
            etWeight = findViewById(R.id.etWeight);
            etHeight = findViewById(R.id.etHeight);
            etActivity = findViewById(R.id.etActivity);
            rgGender = findViewById(R.id.rgGender);
            tvBMI = findViewById(R.id.tvBMI);
            tvBMICategory = findViewById(R.id.tvBMICategory);
            tvCalories = findViewById(R.id.tvCalories);
            btnCalculate = findViewById(R.id.btnCalculate);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (validateInput()) {
                        calculateResults();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in calculation: " + e.getMessage());
                    Toast.makeText(BodyIndexActivity.this, 
                        "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInput() {
        try {
            String ageStr = etAge.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            String activityStr = etActivity.getText().toString().trim();

            // Kiểm tra trống
            if (ageStr.isEmpty() || weightStr.isEmpty() || 
                heightStr.isEmpty() || activityStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", 
                    Toast.LENGTH_SHORT).show();
                return false;
            }

            // Parse và kiểm tra giá trị
            int age = Integer.parseInt(ageStr);
            float weight = Float.parseFloat(weightStr);
            float height = Float.parseFloat(heightStr);
            int activity = Integer.parseInt(activityStr);

            // Kiểm tra phạm vi giá trị
            if (age < 15 || age > 80) {
                etAge.setError("Tuổi phải từ 15-80");
                etAge.requestFocus();
                return false;
            }

            if (weight < 30 || weight > 200) {
                etWeight.setError("Cân nặng phải từ 30-200 kg");
                etWeight.requestFocus();
                return false;
            }

            if (height < 140 || height > 220) {
                etHeight.setError("Chiều cao phải từ 140-220 cm");
                etHeight.requestFocus();
                return false;
            }

            if (activity < 1 || activity > 5) {
                etActivity.setError("Mức độ vận động từ 1-5");
                etActivity.requestFocus();
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng số", 
                Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Number format error: " + e.getMessage());
            return false;
        }
    }

    private void calculateResults() {
        try {
            // Lấy giá trị input
            float weight = Float.parseFloat(etWeight.getText().toString().trim());
            float heightCm = Float.parseFloat(etHeight.getText().toString().trim());
            float height = heightCm / 100; // Chuyển cm sang m
            int age = Integer.parseInt(etAge.getText().toString().trim());
            int activityLevel = Integer.parseInt(etActivity.getText().toString().trim());
            boolean isMale = rgGender.getCheckedRadioButtonId() == R.id.rbMale;

            // Tính BMI
            float bmi = calculateBMI(weight, height);
            String bmiCategory = getBMICategory(bmi);
            
            // Tính BMR và TDEE
            double bmr = calculateBMR(weight, heightCm, age, isMale);
            double tdee = calculateTDEE(bmr, activityLevel);

            // Hiển thị kết quả
            displayResults(bmi, bmiCategory, tdee);

        } catch (Exception e) {
            Log.e(TAG, "Calculation error: " + e.getMessage());
            Toast.makeText(this, "Lỗi tính toán, vui lòng thử lại", 
                Toast.LENGTH_SHORT).show();
        }
    }

    private float calculateBMI(float weight, float height) {
        return weight / (height * height);
    }

    private String getBMICategory(float bmi) {
        if (bmi < 18.5) {
            return "Thiếu cân (< 18.5)";
        } else if (bmi < 24.9) {
            return "Bình thường (18.5 - 24.9)";
        } else if (bmi < 29.9) {
            return "Thừa cân (25 - 29.9)";
        } else {
            return "Béo phì (> 30)";
        }
    }

    private double calculateBMR(float weight, float height, int age, boolean isMale) {
        if (isMale) {
            return 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            return 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }
    }

    private double calculateTDEE(double bmr, int activityLevel) {
        double activityFactor;
        switch (activityLevel) {
            case 1: // Ít vận động
                activityFactor = 1.2;
                break;
            case 2: // Vận động nhẹ
                activityFactor = 1.375;
                break;
            case 3: // Vận động vừa
                activityFactor = 1.55;
                break;
            case 4: // Vận động nhiều
                activityFactor = 1.725;
                break;
            case 5: // Vận động mạnh
                activityFactor = 1.9;
                break;
            default:
                activityFactor = 1.2;
        }
        return bmr * activityFactor;
    }

    private void displayResults(float bmi, String bmiCategory, double tdee) {
        DecimalFormat df = new DecimalFormat("#.##");
        
        // Hiển thị BMI
        tvBMI.setText(df.format(bmi));
        tvBMICategory.setText(bmiCategory);
        
        // Hiển thị calo cần thiết (TDEE)
        String caloriesText = String.format("%s kcal/ngày", Math.round(tdee));
        tvCalories.setText(caloriesText);

        // Log kết quả để debug
        Log.d(TAG, "BMI: " + bmi);
        Log.d(TAG, "Category: " + bmiCategory);
        Log.d(TAG, "TDEE: " + tdee);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 