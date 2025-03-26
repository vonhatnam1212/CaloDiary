package com.example.calodiary.model;

public class UserHealth {
    private String userId;
    private double height;
    private double weight;
    private double bmi;
    private double bmr;
    private double dailyCalorieNeeds;
    private String lastUpdateDate;

    public UserHealth() {
        // Empty constructor for Firestore
    }

    public UserHealth(String userId, double height, double weight) {
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        calculateHealthMetrics();
    }

    // Getters
    public String getUserId() { return userId; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public double getBmi() { return bmi; }
    public double getBmr() { return bmr; }
    public double getDailyCalorieNeeds() { return dailyCalorieNeeds; }
    public String getLastUpdateDate() { return lastUpdateDate; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setHeight(double height) { 
        this.height = height;
        calculateHealthMetrics();
    }
    public void setWeight(double weight) { 
        this.weight = weight;
        calculateHealthMetrics();
    }
    public void setBmi(double bmi) { this.bmi = bmi; }
    public void setBmr(double bmr) { this.bmr = bmr; }
    public void setDailyCalorieNeeds(double dailyCalorieNeeds) { this.dailyCalorieNeeds = dailyCalorieNeeds; }
    public void setLastUpdateDate(String lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    // Calculate BMI, BMR and daily calorie needs
    private void calculateHealthMetrics() {
        // Calculate BMI
        double heightInMeters = height / 100.0; // Convert cm to meters
        bmi = weight / (heightInMeters * heightInMeters);

        // Calculate BMR (using Mifflin-St Jeor Equation)
        bmr = (10 * weight) + (6.25 * height) - 5;

        // Calculate daily calorie needs (using moderate activity multiplier of 1.55)
        dailyCalorieNeeds = bmr * 1.55;
    }
} 