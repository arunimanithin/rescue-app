package com.rescueapp.gui;

import com.rescueapp.core.User; // Import User class
import com.rescueapp.db.dao.RescueAppUserDAO; // Import DAO

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException; // Import SQLException
import java.util.UUID; // Import UUID for generating ID

@SuppressWarnings("serial")
public class UserRegistrationPage extends JFrame {
    private JTextField nameField, emailField, contactField;
    private JComboBox<String> roleComboBox;
    private JButton submitButton;
    private RescueAppUserDAO userDao; // Store the DAO instance

    // Constructor accepts the DAO
    public UserRegistrationPage(RescueAppUserDAO dao) {
        this.userDao = dao;

        setTitle("User Registration");
        setSize(500, 450);
        setLocationRelativeTo(null); // Center on screen
        // Close only this window, not the whole app
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 225, 255)); // light lavender
        add(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        
        JLabel titleLabel = new JLabel("Create New Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(100, 50, 140));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        
        gbc.gridy++;
        mainPanel.add(createLabel("Name:", labelFont), gbc);
        nameField = createTextField(fieldFont);
        gbc.gridx = 1; mainPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(createLabel("Email:", labelFont), gbc);
        emailField = createTextField(fieldFont);
        gbc.gridx = 1; mainPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(createLabel("Contact:", labelFont), gbc);
        contactField = createTextField(fieldFont);
        gbc.gridx = 1; mainPanel.add(contactField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(createLabel("Role:", labelFont), gbc);
        
        
        String[] roles = {"User", "Volunteer", "NGO"}; 
        
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(fieldFont);
        gbc.gridx = 1; mainPanel.add(roleComboBox, gbc);

        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        submitButton = createStyledButton("Register");
        mainPanel.add(submitButton, gbc);

        
        submitButton.addActionListener(this::handleSubmit);
    }

    
    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(90, 60, 120));
        return label;
    }

    private JTextField createTextField(Font font) {
        JTextField textField = new JTextField(15);
        textField.setFont(font);
        textField.setBorder(BorderFactory.createLineBorder(new Color(180, 140, 220), 1, true));
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(new Color(220, 180, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(160, 120, 210), 2, true));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    
    private void handleSubmit(ActionEvent e) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String userId = UUID.randomUUID().toString(); // Generate ID
        User newUser = new User(userId, name, email, role, contact);

        try {
            userDao.addUser(newUser);

            JOptionPane.showMessageDialog(this,
                    "User Registered Successfully!\n\n" +
                            "Generated User ID: " + userId +
                            "\nName: " + name +
                            "\nEmail: " + email +
                            "\nContact: " + contact +
                            "\nRole: " + role +
                            "\n\nPlease use this User ID to log in.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            this.dispose(); // Close registration window

        } catch (SQLException ex) {
            ex.printStackTrace();
            
            if (ex.getMessage().contains("Duplicate entry")) {
                 JOptionPane.showMessageDialog(this, "Registration failed. Email or Contact already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Registration failed due to a database error.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) { 
             ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "An unexpected error occurred during registration.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}