package com.example.calodiary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class BodyIndexActivity extends AppCompatActivity {
    private EditText etAge, etWeight, etHeight, etActivity;
    private RadioGroup rgGender;
    private TextView tvBMI, tvBMICategory, tvCalories;
    private Button btnCalculate;
    private LinearLayout resultLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_index);
        initializeViews();
        setupClickListeners();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etActivity = findViewById(R.id.etActivity);
        rgGender = findViewById(R.id.rgGender);
        tvBMI = findViewById(R.id.tvBMI);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        tvCalories = findViewById(R.id.tvCalories);
        btnCalculate = findViewById(R.id.btnCalculate);
        resultLayout = findViewById(R.id.resultLayout);
    }

    private void setupClickListeners() {
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    calculateAndDisplayResults();
                }
            }
        });
    }

    private boolean validateInput() {
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String activityStr = etActivity.getText().toString().trim();

        // Kiểm tra trống
        if (ageStr.isEmpty()) {
            etAge.setError("Vui lòng nhập tuổi");
            return false;
        }
        if (weightStr.isEmpty()) {
            etWeight.setError("Vui lòng nhập cân nặng");
            return false;
        }
        if (heightStr.isEmpty()) {
            etHeight.setError("Vui lòng nhập chiều cao");
            return false;
        }
        if (activityStr.isEmpty()) {
            etActivity.setError("Vui lòng nhập mức độ vận động");
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float weight = Float.parseFloat(weightStr);
            float height = Float.parseFloat(heightStr);
            int activity = Integer.parseInt(activityStr);

            if (age < 15 || age > 80) {
                etAge.setError("Tuổi phải từ 15-80");
                return false;
            }
            if (weight < 30 || weight > 200) {
                etWeight.setError("Cân nặng phải từ 30-200 kg");
                return false;
            }
            if (height < 140 || height > 220) {
                etHeight.setError("Chiều cao phải từ 140-220 cm");
                return false;
            }
            if (activity < 1 || activity > 5) {
                etActivity.setError("Mức độ vận động từ 1-5");
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void calculateAndDisplayResults() {
        try {
            float weight = Float.parseFloat(etWeight.getText().toString());
            float height = Float.parseFloat(etHeight.getText().toString()) / 100; // Convert to meters
            int age = Integer.parseInt(etAge.getText().toString());
            int activity = Integer.parseInt(etActivity.getText().toString());
            boolean isMale = rgGender.getCheckedRadioButtonId() == R.id.rbMale;

            // Calculate BMI
            float bmi = weight / (height * height);

            // Calculate BMR
            double bmr;
            if (isMale) {
                bmr = 88.362 + (13.397 * weight) + (4.799 * height * 100) - (5.677 * age);
            } else {
                bmr = 447.593 + (9.247 * weight) + (3.098 * height * 100) - (4.330 * age);
            }

            // Calculate TDEE
            double activityFactor;
            switch (activity) {
                case 1: activityFactor = 1.2; break;
                case 2: activityFactor = 1.375; break;
                case 3: activityFactor = 1.55; break;
                case 4: activityFactor = 1.725; break;
                case 5: activityFactor = 1.9; break;
                default: activityFactor = 1.2;
            }
            double tdee = bmr * activityFactor;

            // Display results
            DecimalFormat df = new DecimalFormat("#.##");
            tvBMI.setText(df.format(bmi));
            
            // Set BMI category
            String category;
            if (bmi < 18.5) {
                category = "Thiếu cân";
            } else if (bmi < 24.9) {
                category = "Bình thường";
            } else if (bmi < 29.9) {
                category = "Thừa cân";
            } else {
                category = "Béo phì";
            }
            tvBMICategory.setText(category);
            
            // Set calories
            tvCalories.setText(Math.round(tdee) + " kcal/ngày");

            // Show results
            resultLayout.setVisibility(View.VISIBLE);

            // Lưu kết quả
            saveResultsToFirebase(bmi, category, tdee, weight, height, age, activity, isMale);

        } catch (Exception e) {
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveResultsToFirebase(float bmi, String category, double tdee,
                                     float weight, float height, int age, 
                                     int activity, boolean isMale) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        UserHealth userHealth = new UserHealth(
            currentUser.getUid(),
            bmi,
            category,
            tdee,
            weight,
            height,
            age,
            activity,
            isMale
        );

        db.collection("user_health")
            .document(currentUser.getUid())
            .set(userHealth)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
                showResultDialog(bmi, category, tdee);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @SuppressLint("DefaultLocale")
    private void showResultDialog(float bmi, String category, double tdee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kết quả tính toán")
               .setMessage(String.format(
                   "BMI: %.2f\nPhân loại: %s\nLượng calo cần thiết: %.0f kcal/ngày",
                   bmi, category, tdee))
               .setPositiveButton("Lập thực đơn", (dialog, which) -> {
                   Intent intent = new Intent( BodyIndexActivity.this, MealPlanActivity.class);
                   startActivity(intent);
               })
               .setNegativeButton("Đóng", null)
               .show();
    }
} 