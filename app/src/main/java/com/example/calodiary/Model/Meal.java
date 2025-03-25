package com.example.calodiary.model;

public class Meal {
    private int type;
    private String name;
    private double calories;
    private double portion;
    private String time;
    private String description;

    public Meal() {
        // Empty constructor for Firestore
    }

    public Meal(int type, String name, double calories, double portion) {
        this.type = type;
        this.name = name;
        this.calories = calories;
        this.portion = portion;
        this.time = "";         // Default value
        this.description = "";  // Default value
    }

    public Meal(String name, double calories, String time, String description) {
        this.type = 0;         // Default value
        this.name = name;
        this.calories = calories;
        this.portion = 1.0;    // Default value
        this.time = time;
        this.description = description;
    }

    // Getters
    public int getType() { return type; }
    public String getName() { return name; }
    public double getCalories() { return calories; }
    public double getPortion() { return portion; }
    public String getTime() { return time; }
    public String getDescription() { return description; }

    // Setters
    public void setType(int type) { this.type = type; }
    public void setName(String name) { this.name = name; }
    public void setCalories(double calories) { this.calories = calories; }
    public void setPortion(double portion) { this.portion = portion; }
    public void setTime(String time) { this.time = time; }
    public void setDescription(String description) { this.description = description; }
} 