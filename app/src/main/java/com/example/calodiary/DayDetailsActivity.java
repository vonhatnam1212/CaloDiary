package com.example.calodiary;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DayDetailsActivity extends AppCompatActivity {
    private RecyclerView rvMeals;
    private RecentMealsAdapter adapter;
    private List<Meal> meals;
    private TextView tvDate;
    private TextView tvTotalCalories;
    private FirebaseFirestore db;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get selected date from intent
        selectedDate = getIntent().getStringExtra("date");

        // Initialize views
        rvMeals = findViewById(R.id.rvMeals);
        tvDate = findViewById(R.id.tvDate);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);

        // Set up RecyclerView
        meals = new ArrayList<>();
        adapter = new RecentMealsAdapter(meals);
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        rvMeals.setAdapter(adapter);

        // Display date
        tvDate.setText(selectedDate);

        // Load meals for the selected date
        loadMeals();
    }

    private void loadMeals() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("meals")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    meals.clear();
                    int totalCalories = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String type = document.getString("type");
                        String name = document.getString("name");
                        int calories = document.getLong("calories").intValue();
                        totalCalories += calories;

                        meals.add(new Meal(type, name, calories));
                    }

                    adapter.notifyDataSetChanged();
                    tvTotalCalories.setText(String.format("Total Calories: %d", totalCalories));
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
} 