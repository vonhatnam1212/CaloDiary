package com.example.calodiary.model;

public class UserHealth {
    private String userId;
    private float bmi;
    private String bmiCategory;
    private double dailyCalories;
    private float weight;
    private float height;
    private int age;
    private int activityLevel;
    private boolean isMale;
    private long timestamp;

    // Empty constructor for Firebase
    public UserHealth() {}

    // Constructor with parameters
    public UserHealth(String userId, float bmi, String bmiCategory, double dailyCalories,
                     float weight, float height, int age, int activityLevel, boolean isMale) {
        this.userId = userId;
        this.bmi = bmi;
        this.bmiCategory = bmiCategory;
        this.dailyCalories = dailyCalories;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.activityLevel = activityLevel;
        this.isMale = isMale;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
} 