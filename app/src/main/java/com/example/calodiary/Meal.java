package com.example.calodiary;

public class Meal {
    private String type;
    private String name;
    private int calories;

    public Meal(String type, String name, int calories) {
        this.type = type;
        this.name = name;
        this.calories = calories;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
} 