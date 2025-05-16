package com.computercafe.database;

import com.computercafe.model.User;
import com.computercafe.model.Order;
import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/computercafe";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin1234";
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
            initializeDatabase();
        } catch (Exception e) {
            showError("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    private static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "id VARCHAR(10) PRIMARY KEY, " +
                         "username VARCHAR(50) UNIQUE NOT NULL, " +
                         "password VARCHAR(100) NOT NULL, " +
                         "balance DECIMAL(10,2) NOT NULL DEFAULT 0.0, " +
                         "active BOOLEAN NOT NULL DEFAULT true, " +
                         "time_left_seconds INT NOT NULL DEFAULT 0)");
            
            // Create orders table
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                         "id VARCHAR(10) PRIMARY KEY, " +
                         "user_id VARCHAR(10) NOT NULL REFERENCES users(id), " +
                         "username VARCHAR(50) NOT NULL, " +
                         "items TEXT NOT NULL, " +
                         "total DECIMAL(10,2) NOT NULL, " +
                         "order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                         "status VARCHAR(20) NOT NULL DEFAULT 'Processing')");
            
            // Check if users table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            
            if (userCount == 0) {
                // Add sample users
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users (id, username, password, balance, active, time_left_seconds) VALUES (?, ?, ?, ?, ?, ?)");
                
                // Admin user
                addUser(pstmt, "A1001", "admin", "admin123", 0.0, true, 0);
                
                // Regular users
                addUser(pstmt, "C1001", "user", "user123", 50.0, true, 3600);
                addUser(pstmt, "C1002", "john", "pass123", 25.0, false, 0);
                addUser(pstmt, "C1003", "mary", "pass123", 100.0, true, 7200);
                
                // Add sample orders
                pstmt = conn.prepareStatement(
                    "INSERT INTO orders (id, user_id, username, items, total, order_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)");
                
                addOrder(pstmt, "O1001", "C1001", "user", "Burger, Fries", 8.98, System.currentTimeMillis() - 3600000, "Completed");
                addOrder(pstmt, "O1002", "C1003", "mary", "Pizza, Soda", 5.98, System.currentTimeMillis() - 1800000, "Processing");
            }
        }
    }
    
    private static void addUser(PreparedStatement pstmt, String id, String username, String password, 
                              double balance, boolean active, int timeLeft) throws SQLException {
        pstmt.setString(1, id);
        pstmt.setString(2, username);
        pstmt.setString(3, password);
        pstmt.setDouble(4, balance);
        pstmt.setBoolean(5, active);
        pstmt.setInt(6, timeLeft);
        pstmt.executeUpdate();
    }
    
    private static void addOrder(PreparedStatement pstmt, String id, String userId, String username, 
                               String items, double total, long timeMillis, String status) throws SQLException {
        pstmt.setString(1, id);
        pstmt.setString(2, userId);
        pstmt.setString(3, username);
        pstmt.setString(4, items);
        pstmt.setDouble(5, total);
        pstmt.setTimestamp(6, new Timestamp(timeMillis));
        pstmt.setString(7, status);
        pstmt.executeUpdate();
    }
    
    // User operations
    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id LIKE 'C%' ORDER BY id")) {
            
            while (rs.next()) {
                users.add(new User(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getDouble("balance"),
                    rs.getBoolean("active"),
                    rs.getInt("time_left_seconds")
                ));
            }
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
        }
        return users;
    }
    
    public static User findUserById(String id) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getDouble("balance"),
                    rs.getBoolean("active"),
                    rs.getInt("time_left_seconds")
                );
            }
        } catch (SQLException e) {
            showError("Error finding user: " + e.getMessage());
        }
        return null;
    }
    
    public static User findUserByUsername(String username) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getDouble("balance"),
                    rs.getBoolean("active"),
                    rs.getInt("time_left_seconds")
                );
            }
        } catch (SQLException e) {
            showError("Error finding user: " + e.getMessage());
        }
        return null;
    }
    
    public static String generateUserId() {
        String nextId = "C1001";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(CAST(SUBSTRING(id, 2) AS INTEGER)) FROM users WHERE id LIKE 'C%'")) {
            
            if (rs.next() && rs.getString(1) != null) {
                nextId = "C" + (rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            showError("Error generating user ID: " + e.getMessage());
        }
        return nextId;
    }
    
    public static void addUser(User user) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO users (id, username, password, balance, active, time_left_seconds) VALUES (?, ?, ?, ?, ?, ?)")) {
            
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setDouble(4, user.getBalance());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setInt(6, user.getTimeLeftSeconds());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showError("Error adding user: " + e.getMessage());
        }
    }
    
    public static void updateUser(User user) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE users SET balance = ?, active = ?, time_left_seconds = ? WHERE id = ?")) {
            
            pstmt.setDouble(1, user.getBalance());
            pstmt.setBoolean(2, user.isActive());
            pstmt.setInt(3, user.getTimeLeftSeconds());
            pstmt.setString(4, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showError("Error updating user: " + e.getMessage());
        }
    }
    
    // Order operations
    public static List<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders ORDER BY order_time DESC")) {
            
            while (rs.next()) {
                orders.add(new Order(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("items"),
                    rs.getDouble("total"),
                    rs.getTimestamp("order_time"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            showError("Error loading orders: " + e.getMessage());
        }
        return orders;
    }
    
    public static String generateOrderId() {
        String nextId = "O1001";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(CAST(SUBSTRING(id, 2) AS INTEGER)) FROM orders")) {
            
            if (rs.next() && rs.getString(1) != null) {
                nextId = "O" + (rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            showError("Error generating order ID: " + e.getMessage());
        }
        return nextId;
    }
    
    public static void addOrder(Order order) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO orders (id, user_id, username, items, total, order_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            
            pstmt.setString(1, order.getId());
            pstmt.setString(2, order.getUserId());
            pstmt.setString(3, order.getUsername());
            pstmt.setString(4, order.getItems());
            pstmt.setDouble(5, order.getTotal());
            pstmt.setTimestamp(6, new Timestamp(order.getOrderTime().getTime()));
            pstmt.setString(7, order.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showError("Error adding order: " + e.getMessage());
        }
    }
    
    public static void updateOrderStatus(String orderId, String status) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE orders SET status = ? WHERE id = ?")) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showError("Error updating order status: " + e.getMessage());
        }
    }
}