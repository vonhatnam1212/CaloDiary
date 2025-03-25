public class DailyMeal {
    private String userId;
    private String date; // Format: dd/MM/yyyy
    private List<Meal> meals;
    private double totalCalories;
    private long timestamp;

    public DailyMeal() {}

    public DailyMeal(String userId, String date, List<Meal> meals) {
        this.userId = userId;
        this.date = date;
        this.meals = meals;
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

    // Getters and setters
} 