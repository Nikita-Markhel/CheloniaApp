package com.example.chelonia.information;

public class Note {
    private final String title;
    private final String description;
    private final String timestamp;
    private final boolean isHourlyRate;
    private final double amount;
    private final double hourlyRate;
    private final int hoursWorked;

    public Note(
            String title,
            String description,
            String timestamp,
            boolean isHourlyRate,
            double amount,
            double hourlyRate,
            int hoursWorked
    ) {
        this.title = title;
        this.timestamp = timestamp;
        this.description = description;
        this.isHourlyRate = isHourlyRate;
        this.amount = amount;
        this.hourlyRate = hourlyRate;
        this.hoursWorked = hoursWorked;
    }

    public String getTitle() {
        return title;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription(){
        return description;
    }

    public boolean isHourlyRate() {
        return isHourlyRate;
    }

    public double getAmount() {
        return amount;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }
}
