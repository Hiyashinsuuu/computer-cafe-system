package com.computercafe.model;

public class User {
    private String id;
    private String username;
    private String password;
    private double balance;
    private boolean active;
    private int timeLeftSeconds;

    public User(String id, String username, String password, double balance, boolean active, int timeLeftSeconds) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.active = active;
        this.timeLeftSeconds = timeLeftSeconds;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public double getBalance() { return balance; }
    public boolean isActive() { return active; }
    public int getTimeLeftSeconds() { return timeLeftSeconds; }

    public String getFormattedTimeLeft() {
        int hours = timeLeftSeconds / 3600;
        int minutes = (timeLeftSeconds % 3600) / 60;
        int secs = timeLeftSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public void setBalance(double balance) { this.balance = balance; }
    public void setActive(boolean active) { this.active = active; }
    public void setTimeLeftSeconds(int seconds) { this.timeLeftSeconds = seconds; }
}