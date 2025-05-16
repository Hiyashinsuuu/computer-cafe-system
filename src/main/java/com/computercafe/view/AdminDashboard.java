package com.computercafe.view;

import com.computercafe.database.DatabaseManager;
import com.computercafe.model.User;
import com.computercafe.model.Order;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class AdminDashboard extends JFrame {
    private JTable usersTable;
    private DefaultTableModel userTableModel;
    private JTable ordersTable;
    private DefaultTableModel orderTableModel;
    private JTabbedPane tabbedPane;

    public AdminDashboard() {
        setTitle("Computer Café System - Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Users", createUsersPanel());
        tabbedPane.addTab("Orders", createOrdersPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"ID", "Username", "Balance", "Status", "Time Left"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        updateUserTable();

        usersTable = new JTable(userTableModel);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addButton = new JButton("Add User");
        addButton.addActionListener(e -> showAddUserDialog());

        JButton editButton = new JButton("Edit User");
        editButton.addActionListener(e -> {
            int row = usersTable.getSelectedRow();
            if (row >= 0) {
                String userId = (String) userTableModel.getValueAt(row, 0);
                User user = DatabaseManager.findUserById(userId);
                if (user != null) showEditUserDialog(user);
            } else {
                JOptionPane.showMessageDialog(this, "Select a user first", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateUserTable());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateUserTable() {
        userTableModel.setRowCount(0);
        List<User> users = DatabaseManager.getUsers();
        for (User user : users) {
            Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    String.format("₱%.2f", user.getBalance()),
                    user.isActive() ? "Active" : "Inactive",
                    user.getFormattedTimeLeft()
            };
            userTableModel.addRow(row);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField balanceField = new JTextField("0.00");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        JTextField timeField = new JTextField("00:00");

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Balance:"));
        panel.add(balanceField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Initial Time (HH:MM):"));
        panel.add(timeField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                double balance = Double.parseDouble(balanceField.getText());
                boolean active = statusCombo.getSelectedItem().equals("Active");
                String[] timeParts = timeField.getText().split(":");
                int timeSeconds = Integer.parseInt(timeParts[0]) * 3600 + Integer.parseInt(timeParts[1]) * 60;

                if (username.isEmpty() || password.isEmpty()) {
                    throw new IllegalArgumentException("Username/password cannot be empty");
                }

                User newUser = new User(
                        DatabaseManager.generateUserId(),
                        username,
                        password,
                        balance,
                        active,
                        timeSeconds
                );

                DatabaseManager.addUser(newUser);
                updateUserTable();
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditUserDialog(User user) {
        JDialog dialog = new JDialog(this, "Edit User: " + user.getUsername(), true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField idField = new JTextField(user.getId());
        idField.setEditable(false);
        JTextField usernameField = new JTextField(user.getUsername());
        usernameField.setEditable(false);
        JTextField balanceField = new JTextField(String.format("%.2f", user.getBalance()));
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusCombo.setSelectedItem(user.isActive() ? "Active" : "Inactive");
        JTextField timeField = new JTextField(user.getFormattedTimeLeft());

        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Balance:"));
        panel.add(balanceField);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Time Left (HH:MM:SS):"));
        panel.add(timeField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                double balance = Double.parseDouble(balanceField.getText());
                boolean active = statusCombo.getSelectedItem().equals("Active");
                String[] timeParts = timeField.getText().split(":");
                int timeSeconds = Integer.parseInt(timeParts[0]) * 3600
                        + Integer.parseInt(timeParts[1]) * 60
                        + Integer.parseInt(timeParts[2]);

                user.setBalance(balance);
                user.setActive(active);
                user.setTimeLeftSeconds(timeSeconds);

                DatabaseManager.updateUser(user);
                updateUserTable();
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        String[] columnNames = {"Order ID", "User ID", "Username", "Items", "Total", "Time", "Status"};
        orderTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        updateOrderTable();

        ordersTable = new JTable(orderTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateOrderTable());

        JButton statusButton = new JButton("Update Status");
        statusButton.addActionListener(e -> {
            int row = ordersTable.getSelectedRow();
            if (row >= 0) {
                String orderId = (String) orderTableModel.getValueAt(row, 0);
                updateOrderStatus(orderId);
            } else {
                JOptionPane.showMessageDialog(this, "Select an order first", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(statusButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateOrderTable() {
        orderTableModel.setRowCount(0);
        List<Order> orders = DatabaseManager.getOrders();
        for (Order order : orders) {
            Object[] row = {
                    order.getId(),
                    order.getUserId(),
                    order.getUsername(),
                    order.getItems(),
                    String.format("$%.2f", order.getTotal()),
                    order.getFormattedTime(),
                    order.getStatus()
            };
            orderTableModel.addRow(row);
        }
    }

    private void updateOrderStatus(String orderId) {
        List<Order> orders = DatabaseManager.getOrders();
        Order target = null;
        for (Order order : orders) {
            if (order.getId().equals(orderId)) {
                target = order;
                break;
            }
        }

        if (target != null) {
            String[] statuses = {"Processing", "Ready", "Delivered", "Completed", "Cancelled"};
            String currentStatus = target.getStatus();

            String newStatus = (String) JOptionPane.showInputDialog(
                    this,
                    "Update status for Order #" + orderId,
                    "Order Status",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    statuses,
                    currentStatus
            );

            if (newStatus != null && !newStatus.equals(currentStatus)) {
                DatabaseManager.updateOrderStatus(orderId, newStatus);
                updateOrderTable();
            }
        }
    }

    private void logout() {
        new LoginForm().setVisible(true);
        dispose();
    }
}