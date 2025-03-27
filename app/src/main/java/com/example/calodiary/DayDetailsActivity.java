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
        RecyclerView recyclerView = findViewById(R.id.rvMeals);
        tvDate = findViewById(R.id.tvDate);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);

        // Khởi tạo adapter không có tham số
        adapter = new RecentMealsAdapter();
        
        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Display date
        tvDate.setText(selectedDate);

        // Khởi tạo danh sách meals
        meals = new ArrayList<>();
        
        // Load dữ liệu meals (nếu có)
        loadMeals();
    }

    private void loadMeals() {
        // Load dữ liệu meals từ nguồn của bạn
        // Ví dụ: từ Intent hoặc Database
        
        // Sau khi có dữ liệu, cập nhật adapter
        adapter.setMeals(meals);
    }
} 