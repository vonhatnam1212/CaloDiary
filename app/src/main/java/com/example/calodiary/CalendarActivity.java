package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private TextView tvWeekRange;
    private ListView listViewDays;
    private FirebaseFirestore db;
    private List<DayData> weekData;
    private double dailyCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvWeekRange = findViewById(R.id.tvWeekRange);
        listViewDays = findViewById(R.id.listViewDays);

        // Initialize data
        weekData = new ArrayList<>();
        loadUserData();
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User userData = documentSnapshot.toObject(User.class);
                    if (userData != null) {
                        dailyCalories = calculateDailyCalories(userData);
                        setupWeekView();
                    }
                });
    }

    private double calculateDailyCalories(User userData) {
        // Mifflin-St Jeor Equation
        double bmr;
        if (userData.getGender().equals("Male")) {
            bmr = (10 * userData.getWeight()) + (6.25 * userData.getHeight()) - (5 * userData.getAge()) + 5;
        } else {
            bmr = (10 * userData.getWeight()) + (6.25 * userData.getHeight()) - (5 * userData.getAge()) - 161;
        }
        return bmr * Double.parseDouble(userData.getActivityLevel());
    }

    private void setupWeekView() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        // Get start of week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        Date startDate = calendar.getTime();
        
        // Get end of week
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date endDate = calendar.getTime();
        
        tvWeekRange.setText(String.format("%s - %s", dateFormat.format(startDate), dateFormat.format(endDate)));

        // Load data for each day
        calendar.setTime(startDate);
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            loadDayData(date);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        listViewDays.setAdapter(new WeekAdapter());
    }

    private void loadDayData(String date) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("meals")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalCalories = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        totalCalories += document.getLong("calories").intValue();
                    }
                    weekData.add(new DayData(date, totalCalories));
                    ((WeekAdapter) listViewDays.getAdapter()).notifyDataSetChanged();
                });
    }

    private class DayData {
        String date;
        int calories;

        DayData(String date, int calories) {
            this.date = date;
            this.calories = calories;
        }
    }

    private class WeekAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return weekData.size();
        }

        @Override
        public Object getItem(int position) {
            return weekData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_day, parent, false);
            }

            DayData dayData = weekData.get(position);
            TextView tvDate = convertView.findViewById(R.id.tvDate);
            TextView tvCalories = convertView.findViewById(R.id.tvCalories);
            TextView tvStatus = convertView.findViewById(R.id.tvStatus);

            tvDate.setText(dayData.date);
            tvCalories.setText(String.format("Calories: %d/%d", dayData.calories, (int) dailyCalories));

            if (dayData.calories >= dailyCalories) {
                tvStatus.setText("Goal Achieved");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Goal Not Met");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(CalendarActivity.this, DayDetailsActivity.class);
                intent.putExtra("date", dayData.date);
                startActivity(intent);
            });

            return convertView;
        }
    }
}