package com.example.calodiary.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.calodiary.R;
import com.example.calodiary.models.Meal;
import java.util.List;

public class DayMealsAdapter extends RecyclerView.Adapter<DayMealsAdapter.MealViewHolder> {
    private List<Meal> meals;

    public DayMealsAdapter(List<Meal> meals) {
        this.meals = meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMealName;
        private TextView tvCalories;
        private TextView tvMealType;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvMealType = itemView.findViewById(R.id.tvMealType);
        }

        public void bind(Meal meal) {
            tvMealName.setText(meal.getName());
            tvCalories.setText(meal.getCalories() + " cal");
            tvMealType.setText(meal.getType());
        }
    }
} 