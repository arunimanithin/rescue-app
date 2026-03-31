package com.rescueapp.gui.user;

import com.rescueapp.core.Report;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppNotificationDAO;
import com.rescueapp.db.dao.RescueAppReportDAO;
import com.rescueapp.db.dao.RescueAppUserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class ReportAnimalPanel extends JPanel {

    private JTextArea descriptionArea;
    private JTextField locationField;
    private JTextField photoUrlField;
    private JComboBox<String> urgencyComboBox;
    private JButton submitButton;

    private RescueAppReportDAO reportDAO;
    private RescueAppUserDAO userDAO;
    private RescueAppNotificationDAO notificationDAO;
    private User loggedInUser;

    private final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();

    public ReportAnimalPanel(User user) {
        super(new BorderLayout(10, 10));
        this.loggedInUser = user;

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            reportDAO = new RescueAppReportDAO(db);
            userDAO = new RescueAppUserDAO(db);
            notificationDAO = new RescueAppNotificationDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Form Panel using GridBagLayout ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Title
        JLabel titleLabel = new JLabel("Report a Stray Animal in Need");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 5, 15, 5);
        formPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(5, 5, 5, 5);

        // Description
        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createLabel("Description:", labelFont), gbc);
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        formPanel.add(scrollPane, gbc);
        gbc.weightx = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Location
        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(createLabel("Location:", labelFont), gbc);
        locationField = createTextField(fieldFont);
        gbc.gridx = 1; formPanel.add(locationField, gbc);

        // Photo URL
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createLabel("Photo URL (optional):", labelFont), gbc);
        photoUrlField = createTextField(fieldFont);
        gbc.gridx = 1; formPanel.add(photoUrlField, gbc);

        // Urgency
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(createLabel("Urgency:", labelFont), gbc);
        String[] urgencyLevels = {"Low", "Medium", "High", "Critical"};
        urgencyComboBox = new JComboBox<>(urgencyLevels);
        urgencyComboBox.setFont(fieldFont);
        gbc.gridx = 1; formPanel.add(urgencyComboBox, gbc);

        // Submit Button
        submitButton = new JButton("Submit Report");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitButton.setBackground(new Color(0, 120, 200));
        submitButton.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(submitButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        submitButton.addActionListener(this::handleSubmitReport);
    }

    private void handleSubmitReport(ActionEvent e) {
        String description = descriptionArea.getText().trim();
        String location = locationField.getText().trim();
        String photoUrl = photoUrlField.getText().trim();
        String urgency = (String) urgencyComboBox.getSelectedItem();

        if (description.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description and Location are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (reportDAO == null || userDAO == null || notificationDAO == null) {
            JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Report report = new Report();
        report.setReportId(UUID.randomUUID().toString());
        report.setDescription(description);
        report.setLocation(location);
        report.setPhotoUrl(photoUrl);
        report.setUrgency(urgency);
        report.setDate(new Date());
        report.setReporter(loggedInUser);
        report.setStatus("Open");

        try {
            reportDAO.addReport(report);
            JOptionPane.showMessageDialog(this, "Report submitted successfully! NGOs will be notified.", "Report Submitted", JOptionPane.INFORMATION_MESSAGE);
            notifyAllNgos("New stray animal report: " + description.substring(0, Math.min(description.length(), 50)) + "...");
            descriptionArea.setText("");
            locationField.setText("");
            photoUrlField.setText("");
            urgencyComboBox.setSelectedIndex(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to submit report. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void notifyAllNgos(String message) {
        if (userDAO == null || notificationDAO == null || notificationExecutor.isShutdown()) {
            System.err.println("Cannot send NGO notifications: DAO missing or executor shut down.");
            return;
        }
        notificationExecutor.submit(() -> {
            try {
                List<User> allUsers = userDAO.getAllUsers();
                int count = 0;
                for (User user : allUsers) {
                    if (user.getRole().equalsIgnoreCase("NGO")) {
                        try {
                            notificationDAO.addNotification(user.getUserId(), message);
                            count++;
                        } catch (SQLException e) {
                            System.err.println("Failed to send notification to NGO " + user.getUserId() + ": " + e.getMessage());
                        }
                    }
                }
                System.out.println("Sent report notification to " + count + " NGOs.");
            } catch (SQLException e) {
                System.err.println("Failed to fetch users to notify NGOs: " + e.getMessage());
            } catch (Exception e) {
                 System.err.println("Unexpected error notifying NGOs: " + e.getMessage());
                 e.printStackTrace();
            }
        });
    }

    /**
     * Method to shut down the executor service. Called by the parent dashboard.
     * THIS IS THE METHOD THAT WAS MISSING.
     */
    public void shutdownExecutor() {
        if (notificationExecutor != null && !notificationExecutor.isShutdown()) {
             System.out.println("ReportAnimalPanel: Shutting down notification executor...");
            notificationExecutor.shutdown();
             try {
                 if (!notificationExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                     notificationExecutor.shutdownNow();
                 }
             } catch (InterruptedException e) {
                 notificationExecutor.shutdownNow();
                 Thread.currentThread().interrupt(); // Re-interrupt thread
             }
        }
    }

    // --- Helper methods for styling ---
     private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    private JTextField createTextField(Font font) {
        JTextField textField = new JTextField(20);
        textField.setFont(font);
        return textField;
    }
}