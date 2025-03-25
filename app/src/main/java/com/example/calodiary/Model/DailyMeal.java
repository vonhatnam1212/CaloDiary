package com.example.calodiary.model;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class DailyMeal {
    private String userId;
    private String date; // Format: dd/MM/yyyy
    private List<Meal> meals;
    private double totalCalories;
    private long timestamp;

    public DailyMeal() {
        // Empty constructor needed for Firestore
        meals = new ArrayList<>();
    }

    public DailyMeal(String userId, String date, List<Meal> meals) {
        this.userId = userId;
        this.date = date;
        this.meals = meals != null ? meals : new ArrayList<>();
        this.calculateTotalCalories();
        this.timestamp = System.currentTimeMillis();
    }

    private void calculateTotalCalories() {
        this.totalCalories = 0;
        if (meals != null) {
            for (Meal meal : meals) {
                this.totalCalories += meal.getCalories();
            }
        }
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public List<Meal> getMeals() { return meals; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setDate(String date) { this.date = date; }
    public void setMeals(List<Meal> meals) { this.meals = meals; }

    // Helper methods
    public void addMeal(Meal meal) {
        if (meals == null) {
            meals = new ArrayList<>();
        }
        meals.add(meal);
    }

    public double getTotalCalories() {
        if (meals == null) return 0;
        return meals.stream()
                .mapToDouble(Meal::getCalories)
                .sum();
    }
} 