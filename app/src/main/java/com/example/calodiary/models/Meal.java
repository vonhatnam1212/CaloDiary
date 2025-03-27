package com.example.calodiary.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Meal {
    private String id;
    private String name;
    private String type;
    private int calories;
    private String date;
    private String userId;
    private long timestamp;

    // Constructor mặc định cho Firestore
    public Meal() {
        this.timestamp = System.currentTimeMillis();
    }

    public Meal(String name, String type, int calories) {
        this.name = name;
        this.type = type;
        this.calories = calories;
        this.timestamp = System.currentTimeMillis();
        this.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date(this.timestamp));
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 