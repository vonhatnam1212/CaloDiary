package com.example.calodiary.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class DayData {
    private final String date;
    private final Date dateTime;
    private int calories;
    private boolean isSelected;
    private double calorieGoal;
    private List<Meal> meals;

    public DayData(String date, Date dateTime, double calorieGoal) {
        this.date = date;
        this.dateTime = dateTime;
        this.calorieGoal = calorieGoal;
        this.calories = 0;
        this.isSelected = false;
        this.meals = new ArrayList<>();
    }

    public String getDate() { return date; }
    public Date getDateTime() { return dateTime; }
    public int getCalories() { return calories; }
    public boolean isSelected() { return isSelected; }
    
    public void setCalories(int calories) { this.calories = calories; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean isCalorieGoalMet() {
        return calories >= calorieGoal;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        this.calories = meals.stream().mapToInt(Meal::getCalories).sum();
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public double getCalorieGoal() {
        return calorieGoal;
    }
} 