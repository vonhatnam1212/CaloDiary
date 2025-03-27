package com.example.calodiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentMealsAdapter extends RecyclerView.Adapter<RecentMealsAdapter.MealViewHolder> {
    private List<Meal> meals;

    public RecentMealsAdapter(List<Meal> meals) {
        this.meals = meals;
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
        Meal meal = meals.get(position);
        holder.tvMealType.setText(meal.getType());
        holder.tvMealName.setText(meal.getName());
        holder.tvMealCalories.setText(String.format("%d calories", meal.getCalories()));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealType;
        TextView tvMealName;
        TextView tvMealCalories;

        MealViewHolder(View itemView) {
            super(itemView);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealCalories = itemView.findViewById(R.id.tvMealCalories);
        }
    }
} 