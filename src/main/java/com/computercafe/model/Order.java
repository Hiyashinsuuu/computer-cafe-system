package com.computercafe.model;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Order {
    private String id;
    private String userId;
    private String username;
    private String items;
    private double total;
    private Date orderTime;
    private String status;
    
    public Order(String id, String userId, String username, String items, double total, Date orderTime, String status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.items = items;
        this.total = total;
        this.orderTime = orderTime;
        this.status = status;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getItems() { return items; }
    public double getTotal() { return total; }
    public Date getOrderTime() { return orderTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getFormattedTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderTime);
    }
}