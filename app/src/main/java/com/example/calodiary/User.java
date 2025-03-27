package com.example.calodiary;

public class User {
    private String name;
    private int age;
    private double weight;
    private double height;
    private String gender;
    private String activityLevel;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String name, int age, double weight, double height, String gender, String activityLevel) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.gender = gender;
        this.activityLevel = activityLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public double getActivityLevelValue() {
        try {
            return Double.parseDouble(activityLevel);
        } catch (Exception e) {
            return 1.2; // Default activity level if parsing fails
        }
    }
} 