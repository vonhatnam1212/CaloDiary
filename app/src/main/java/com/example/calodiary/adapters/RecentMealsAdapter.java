package com.example.calodiary.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calodiary.R;
import com.example.calodiary.models.Meal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentMealsAdapter extends RecyclerView.Adapter<RecentMealsAdapter.MealViewHolder> {
    private List<Meal> meals;

    public RecentMealsAdapter() {
        this.meals = new ArrayList<>();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_recent_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        if (meals != null && position < meals.size()) {
            holder.bind(meals.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return meals != null ? meals.size() : 0;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMealName;
        private final TextView tvMealType;
        private final TextView tvCalories;
        private final TextView tvDate;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo các ID này khớp với layout item_recent_meal.xml
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(Meal meal) {
            if (meal != null) {
                if (tvMealName != null) {
                    tvMealName.setText(meal.getName());
                }
                if (tvMealType != null) {
                    tvMealType.setText(meal.getType());
                }
                if (tvCalories != null) {
                    tvCalories.setText(String.format(Locale.getDefault(), 
                        "%d calories", meal.getCalories()));
                }
                if (tvDate != null) {
                    tvDate.setText(meal.getDate());
                }
            }
        }
    }
} 