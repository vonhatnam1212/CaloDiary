package com.example.calodiary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class BodyIndexActivity extends AppCompatActivity {
    private EditText etAge;
    private EditText etWeight;
    private EditText etHeight;
    private EditText etActivity;
    private RadioGroup rgGender;
    private TextView tvCalories;
    private Button btnCalculate;
    private LinearLayout resultLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_index);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etActivity = findViewById(R.id.etActivity);
        rgGender = findViewById(R.id.rgGender);
        tvCalories = findViewById(R.id.tvCalories);
        btnCalculate = findViewById(R.id.btnCalculate);
        resultLayout = findViewById(R.id.resultLayout);

        btnCalculate.setOnClickListener(v -> calculateCalories());
    }

    private void calculateCalories() {
        try {
            int age = Integer.parseInt(etAge.getText().toString());
            double weight = Double.parseDouble(etWeight.getText().toString());
            double height = Double.parseDouble(etHeight.getText().toString());
            double activityLevel = Double.parseDouble(etActivity.getText().toString());
            boolean isMale = rgGender.getCheckedRadioButtonId() == R.id.rbMale;

            // Mifflin-St Jeor Equation
            double bmr;
            if (isMale) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }
            double dailyCalories = bmr * activityLevel;

            // Display result
            tvCalories.setText(String.format("%.0f calories", dailyCalories));
            resultLayout.setVisibility(View.VISIBLE);

            // Save to Firebase
            saveUserData(age, weight, height, isMale ? "Male" : "Female", String.valueOf(activityLevel));
        } catch (NumberFormatException e) {
            // Handle invalid input
        }
    }

    private void saveUserData(int age, double weight, double height, String gender, String activityLevel) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("age", age);
        userData.put("weight", weight);
        userData.put("height", height);
        userData.put("gender", gender);
        userData.put("activityLevel", activityLevel);

        db.collection("users").document(userId).set(userData);
    }
}