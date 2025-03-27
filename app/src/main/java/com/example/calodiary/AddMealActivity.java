package com.example.calodiary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.widget.ArrayAdapter;
import com.example.calodiary.models.Meal;
import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;
    private EditText etMealName, etCalories;
    private Spinner spinnerMealType;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initializeViews();
        setupSpinner();
        setupSaveButton();
    }

    private void initializeViews() {
        etMealName = findViewById(R.id.etMealName);
        etCalories = findViewById(R.id.etCalories);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.meal_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveMeal());
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String type = spinnerMealType.getSelectedItem().toString();
        int calories;
        try {
            calories = Integer.parseInt(etCalories.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid calories", Toast.LENGTH_SHORT).show();
            return;
        }

        Meal meal = new Meal(name, type, calories);
        meal.setUserId(userId);
        meal.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date()));

        // Save to Firestore
        db.collection("meals")
            .add(meal)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Meal added successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error adding meal: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
} 