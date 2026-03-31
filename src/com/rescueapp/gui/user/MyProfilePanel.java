package com.rescueapp.gui.user;

import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppUserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class MyProfilePanel extends JPanel {

    private JTextField nameField;    // <-- Changed to JTextField
    private JTextField emailField;   // <-- Changed to JTextField
    private JTextField contactField; // <-- Was already JTextField
    private JLabel roleLabel;        // Role is not editable
    private JLabel userIdValue;      // ID is not editable
    private JButton updateButton;

    private User loggedInUser;
    private RescueAppUserDAO userDAO;

    public MyProfilePanel(User user) {
        super(new BorderLayout(10, 10));
        this.loggedInUser = user;

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            userDAO = new RescueAppUserDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to User DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make fields fill horizontally

        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 5, 20, 5);
        infoPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 8, 8, 8); // Reset

        // User ID (read-only)
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; // Label doesn't fill
        infoPanel.add(createLabel("User ID:", labelFont), gbc);
        userIdValue = new JLabel(loggedInUser.getUserId());
        userIdValue.setFont(fieldFont);
        gbc.gridx = 1; infoPanel.add(userIdValue, gbc);

        // Name (Editable)
        gbc.gridx = 0; gbc.gridy++;
        infoPanel.add(createLabel("Name:", labelFont), gbc);
        nameField = new JTextField(loggedInUser.getName(), 20); // <-- JTextField
        nameField.setFont(fieldFont);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; // Field fills
        infoPanel.add(nameField, gbc);

        // Email (Editable)
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE;
        infoPanel.add(createLabel("Email:", labelFont), gbc);
        emailField = new JTextField(loggedInUser.getEmail(), 20); // <-- JTextField
        emailField.setFont(fieldFont);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(emailField, gbc);

        // Contact (Editable)
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE;
        infoPanel.add(createLabel("Contact:", labelFont), gbc);
        contactField = new JTextField(loggedInUser.getContact(), 20); // <-- JTextField
        contactField.setFont(fieldFont);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(contactField, gbc);
        
        // Role (read-only)
        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE;
        infoPanel.add(createLabel("Role:", labelFont), gbc);
        roleLabel = new JLabel(loggedInUser.getRole());
        roleLabel.setFont(fieldFont);
        gbc.gridx = 1; infoPanel.add(roleLabel, gbc);

        // Update Button
        updateButton = new JButton("Update Profile");
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 5, 5, 5);
        infoPanel.add(updateButton, gbc);

        add(infoPanel, BorderLayout.CENTER);

        updateButton.addActionListener(this::handleUpdateProfile);
    }

    private void handleUpdateProfile(ActionEvent e) {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newContact = contactField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty() || newContact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email, and Contact cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simple email format check (basic)
        if (!newEmail.contains("@") || !newEmail.contains(".")) {
             JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO == null) {
            JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update the user object
        loggedInUser.setName(newName);
        loggedInUser.setEmail(newEmail);
        loggedInUser.setContact(newContact);
        // Role remains unchanged

        try {
            boolean success = userDAO.updateUser(loggedInUser); // Assumes updateUser handles all fields

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Optionally update the Dashboard title if name changed
                 Window window = SwingUtilities.getWindowAncestor(this);
                 if (window instanceof JFrame) {
                     ((JFrame) window).setTitle(loggedInUser.getRole() + " Dashboard - Welcome, " + loggedInUser.getName());
                 }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                 // Consider reloading original data if update failed
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Database error during update: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             // Consider reloading original data if update failed
        }
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }
}