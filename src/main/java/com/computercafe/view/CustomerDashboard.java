package com.computercafe.view;

import com.computercafe.database.DatabaseManager;
import com.computercafe.model.User;
import com.computercafe.model.Order;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class CustomerDashboard extends JFrame {
    private JLabel timeLeftLabel;
    private JLabel balanceLabel;
    private int timeLeft;
    private double balance;
    private Timer timer;
    private boolean timeExpiredNotified = false;
    private String username;
    private User currentUser;

    public CustomerDashboard(String username) {
        this.username = username;
        this.currentUser = DatabaseManager.findUserByUsername(username);

        if (currentUser != null) {
            this.timeLeft = currentUser.getTimeLeftSeconds();
            this.balance = currentUser.getBalance();
        }

        setTitle("Computer Café System - Customer: " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Account Information Panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Account Information"));
        infoPanel.add(new JLabel("Time Left:"));
        timeLeftLabel = new JLabel(formatTime(timeLeft));
        infoPanel.add(timeLeftLabel);
        infoPanel.add(new JLabel("Balance:"));
        balanceLabel = new JLabel(String.format("₱%.2f", balance));
        infoPanel.add(balanceLabel);

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton orderButton = new JButton("Order Food");
        orderButton.addActionListener(e -> showFoodOrder());

        JButton addTimeButton = new JButton("Add Time");
        addTimeButton.addActionListener(e -> addTime());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        actionPanel.add(orderButton);
        actionPanel.add(addTimeButton);
        actionPanel.add(logoutButton);

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(actionPanel, BorderLayout.CENTER);

        add(mainPanel);
        startTimer();
    }

    private void showFoodOrder() {
        // Define food items with prices
        String[] foodItems = {
                "Ramen w/ egg - ₱99",
                "Chicken Teriyaki - ₱109",
                "Tapsilog - ₱89"
        };

        // Define image paths for each food item
        String[] foodImagePaths = {
                "src/resources/images/item 1.jpg",
                "src/resources/images/item 2.jpg",
                "src/resources/images/item 3.jpg"
        };

        // Create a custom panel with grid layout for food selection
        JPanel panel = new JPanel(new GridLayout(foodItems.length, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JCheckBox[] checkBoxes = new JCheckBox[foodItems.length];

        for (int i = 0; i < foodItems.length; i++) {
            // Create panel for each food item
            JPanel foodItemPanel = new JPanel(new BorderLayout(10, 5));

            // Add checkbox
            checkBoxes[i] = new JCheckBox(foodItems[i]);
            checkBoxes[i].setFont(new Font("Arial", Font.BOLD, 14));

            // Load and scale image
            ImageIcon originalIcon = new ImageIcon(foodImagePaths[i]);
            Image img = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(img);
            JLabel imageLabel = new JLabel(scaledIcon);
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            // Add components to food item panel
            foodItemPanel.add(imageLabel, BorderLayout.WEST);
            foodItemPanel.add(checkBoxes[i], BorderLayout.CENTER);

            // Add food item panel to main panel
            panel.add(foodItemPanel);
        }

        // Create scroll pane for the panel (in case there are many food items)
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Select Food Items",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            ArrayList<String> selectedItems = new ArrayList<>();
            double totalPrice = 0.0;

            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i].isSelected()) {
                    String item = foodItems[i];
                    selectedItems.add(item.substring(0, item.indexOf(" -")));
                    totalPrice += Double.parseDouble(item.substring(item.indexOf("₱") + 1));
                }
            }

            if (selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No items selected",
                        "Order Cancelled",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            if (totalPrice > balance) {
                JOptionPane.showMessageDialog(
                        this,
                        "Insufficient balance!",
                        "Order Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Create order
            Order newOrder = new Order(
                    DatabaseManager.generateOrderId(),
                    currentUser.getId(),
                    username,
                    String.join(", ", selectedItems),
                    totalPrice,
                    new Date(),
                    "Processing"
            );

            DatabaseManager.addOrder(newOrder);

            // Update balance
            balance -= totalPrice;
            balanceLabel.setText(String.format("₱%.2f", balance));
            currentUser.setBalance(balance);
            DatabaseManager.updateUser(currentUser);

            // Create a panel to display the order summary with images
            JPanel summaryPanel = new JPanel();
            summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel("Your order has been placed!");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            summaryPanel.add(titleLabel);
            summaryPanel.add(Box.createVerticalStrut(10));

            JPanel itemsPanel = new JPanel(new GridLayout(selectedItems.size(), 1, 5, 5));

            // Add selected items with images to summary
            for (String item : selectedItems) {
                int index = -1;
                for (int i = 0; i < foodItems.length; i++) {
                    if (foodItems[i].startsWith(item)) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    JPanel itemPanel = new JPanel(new BorderLayout(5, 0));

                    // Load and scale image for summary
                    ImageIcon originalIcon = new ImageIcon(foodImagePaths[index]);
                    Image img = originalIcon.getImage().getScaledInstance(50, 35, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(img);
                    JLabel imageLabel = new JLabel(scaledIcon);

                    JLabel itemLabel = new JLabel(foodItems[index]);

                    itemPanel.add(imageLabel, BorderLayout.WEST);
                    itemPanel.add(itemLabel, BorderLayout.CENTER);

                    itemsPanel.add(itemPanel);
                }
            }

            summaryPanel.add(itemsPanel);
            summaryPanel.add(Box.createVerticalStrut(10));

            JLabel totalLabel = new JLabel("Total: ₱" + String.format("%.2f", totalPrice));
            totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
            totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            summaryPanel.add(totalLabel);

            // Show order confirmation with images
            JOptionPane.showMessageDialog(
                    this,
                    summaryPanel,
                    "Order Confirmed",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void addTime() {
        String[] timeOptions = {
                "1 Hour - ₱40",
                "3 Hours - ₱100",
                "5 Hours - ₱150"
        };

        String selectedOption = (String) JOptionPane.showInputDialog(
                this,
                "Select time to add:",
                "Add Time",
                JOptionPane.QUESTION_MESSAGE,
                null,
                timeOptions,
                timeOptions[0]
        );

        if (selectedOption != null) {
            int hours = 0;
            double cost = 0.0;

            if (selectedOption.startsWith("1 Hour")) {
                hours = 1;
                cost = 40;
            } else if (selectedOption.startsWith("3 Hours")) {
                hours = 3;
                cost = 100;
            } else if (selectedOption.startsWith("5 Hours")) {
                hours = 5;
                cost = 150;
            }

            if (cost > balance) {
                JOptionPane.showMessageDialog(
                        this,
                        "Insufficient balance!",
                        "Add Time Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                balance -= cost;
                timeLeft += hours * 3600;
                balanceLabel.setText(String.format("₱%.2f", balance));
                timeLeftLabel.setText(formatTime(timeLeft));

                // Update database
                currentUser.setBalance(balance);
                currentUser.setTimeLeftSeconds(timeLeft);
                DatabaseManager.updateUser(currentUser);

                JOptionPane.showMessageDialog(
                        this,
                        "Time added successfully!",
                        "Time Added",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (timeLeft > 0) {
                        timeLeft--;
                        timeLeftLabel.setText(formatTime(timeLeft));
                        currentUser.setTimeLeftSeconds(timeLeft);
                        DatabaseManager.updateUser(currentUser);
                    } else if (!timeExpiredNotified) {
                        timeExpiredNotified = true;

                        JOptionPane.showMessageDialog(
                                CustomerDashboard.this,
                                "Your time has expired! You will be logged out.",
                                "Time Expired",
                                JOptionPane.WARNING_MESSAGE
                        );
                        logout();
                    }
                });
            }
        }, 1000, 1000);
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private void logout() {
        if (timer != null) {
            timer.cancel();
        }
        new LoginForm().setVisible(true);
        dispose();
    }
}