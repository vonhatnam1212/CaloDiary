package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BodyIndexActivity extends AppCompatActivity {
    private EditText etWeight, etHeight;
    private RadioGroup rgGender;
    private TextView tvCalories;
    private Button btnCalculate;
    private LinearLayout resultLayout;
    private FirebaseFirestore db;
    private String userId;
    private TextView tvBMI, tvBMICategory;
    private Button btnSave;
    private AutoCompleteTextView spinnerActivityLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_index);

        try {
            db = FirebaseFirestore.getInstance();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            initializeViews();
            loadLastMeasurement();
            setupCalculateButton();
            setupSaveButton();
            setupActivityLevelSpinner();

        } catch (Exception e) {
            Log.e("BodyIndexActivity", "Error initializing: " + e.getMessage());
            Toast.makeText(this, "Error initializing. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        rgGender = findViewById(R.id.rgGender);
        tvCalories = findViewById(R.id.tvCalories);
        btnCalculate = findViewById(R.id.btnCalculate);
        resultLayout = findViewById(R.id.resultLayout);
        tvBMI = findViewById(R.id.tvBMI);
        tvBMICategory = findViewById(R.id.tvBMICategory);
        btnSave = findViewById(R.id.btnSave);
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel);
    }

    private void loadLastMeasurement() {
        if (userId == null) return;

        db.collection("measurements")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    try {
                        Double weight = documentSnapshot.getDouble("weight");
                        Double height = documentSnapshot.getDouble("height");
                        String gender = documentSnapshot.getString("gender");

                        if (weight != null) etWeight.setText(String.format(Locale.getDefault(), "%.1f", weight));
                        if (height != null) etHeight.setText(String.format(Locale.getDefault(), "%.1f", height));
                        if ("male".equals(gender)) {
                            rgGender.check(R.id.rbMale);
                        } else {
                            rgGender.check(R.id.rbFemale);
                        }
                    } catch (Exception e) {
                        Log.e("BodyIndexActivity", "Error parsing measurement data: " + e.getMessage());
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e("BodyIndexActivity", "Error loading measurement: " + e.getMessage());
                Toast.makeText(this, "Error loading last measurement", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupCalculateButton() {
        btnCalculate.setOnClickListener(v -> {
            if (validateInputs()) {
                calculateBMI();
            }
        });
    }

    private boolean validateInputs() {
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please enter both weight and height", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            float weight = Float.parseFloat(weightStr);
            float height = Float.parseFloat(heightStr);

            if (weight <= 0 || height <= 0) {
                Toast.makeText(this, "Weight and height must be positive numbers", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (weight > 300 || height > 300) {
                Toast.makeText(this, "Please enter realistic values", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void calculateBMI() {
        try {
            float weight = Float.parseFloat(etWeight.getText().toString());
            float height = Float.parseFloat(etHeight.getText().toString()) / 100; // cm to m
            float bmi = weight / (height * height);

            tvBMI.setText(String.format(Locale.getDefault(), "BMI: %.1f", bmi));
            tvBMICategory.setText(getBMICategory(bmi));
            resultLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e("BodyIndexActivity", "Error calculating BMI: " + e.getMessage());
            Toast.makeText(this, "Error calculating BMI", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveMeasurement();
            }
        });
    }

    private void setupActivityLevelSpinner() {
        String[] activityLevels = new String[] {
            "Sedentary (1.2)",
            "Lightly Active (1.375)",
            "Moderately Active (1.55)",
            "Very Active (1.725)",
            "Extra Active (1.9)"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            activityLevels
        );
        spinnerActivityLevel.setAdapter(adapter);
        
        // Set default value
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String activityLevel = document.getString("activityLevel");
                        if (activityLevel != null) {
                            for (int i = 0; i < activityLevels.length; i++) {
                                if (activityLevels[i].contains(activityLevel)) {
                                    spinnerActivityLevel.setText(activityLevels[i], false);
                                    break;
                                }
                            }
                        }
                    }
                });
        }
    }

    private void saveMeasurement() {
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float weight = Float.parseFloat(etWeight.getText().toString());
            float height = Float.parseFloat(etHeight.getText().toString());
            String activityLevelFull = spinnerActivityLevel.getText().toString();
            String activityLevel = extractActivityLevel(activityLevelFull);

            Map<String, Object> measurement = new HashMap<>();
            measurement.put("weight", weight);
            measurement.put("height", height);
            measurement.put("gender", rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "male" : "female");
            measurement.put("activityLevel", activityLevel);
            measurement.put("date", new Date());

            db.collection("measurements")
                .document(userId)
                .set(measurement)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Measurement saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("BodyIndexActivity", "Error saving measurement: " + e.getMessage());
                    Toast.makeText(this, "Error saving measurement", Toast.LENGTH_SHORT).show();
                });
        } catch (Exception e) {
            Log.e("BodyIndexActivity", "Error preparing measurement data: " + e.getMessage());
            Toast.makeText(this, "Error preparing data", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractActivityLevel(String activityLevelFull) {
        // Extract the numeric value from the string (e.g., "Sedentary (1.2)" -> "1.2")
        try {
            return activityLevelFull.substring(
                activityLevelFull.indexOf("(") + 1,
                activityLevelFull.indexOf(")")
            );
        } catch (Exception e) {
            return "1.2"; // Default value
        }
    }

    private String getBMICategory(float bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal weight";
        if (bmi < 30) return "Overweight";
        return "Obesity";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup if needed
    }
}