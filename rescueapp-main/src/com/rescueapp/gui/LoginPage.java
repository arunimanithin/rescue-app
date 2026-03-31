package com.rescueapp.gui;

import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppUserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class LoginPage extends JFrame {
    private JTextField userIdField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;
    private JButton registerButton;

    private RescueAppUserDAO userDao;

    public LoginPage() {
        try {
            RescueAppDbConnector dbConnector = new RescueAppDbConnector();
            userDao = new RescueAppUserDAO(dbConnector);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to database.", "DB Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Stray Animal Rescue - Login");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(230, 240, 255));
        add(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel titleLabel = new JLabel("Login to RescueApp", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(40, 80, 150));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        mainPanel.add(createLabel("User ID:", labelFont), gbc);
        userIdField = createTextField(fieldFont);
        gbc.gridx = 1; mainPanel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(createLabel("Role:", labelFont), gbc);
        String[] roles = {"User", "Volunteer", "NGO", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(fieldFont);
        gbc.gridx = 1; mainPanel.add(roleComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(mainPanel.getBackground());

        loginButton = createStyledButton("Login", new Color(100, 150, 220));
        registerButton = createStyledButton("Register", new Color(80, 180, 80));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

        loginButton.addActionListener(this::handleLogin);
        registerButton.addActionListener(this::handleRegisterOpen);
    }

    private void handleRegisterOpen(ActionEvent e) {
        UserRegistrationPage registrationPage = new UserRegistrationPage(userDao);
        registrationPage.setVisible(true);
    }

    private void handleLogin(ActionEvent e) {
        String userId = userIdField.getText().trim();
        String selectedRole = (String) roleComboBox.getSelectedItem();

        if (userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User user = userDao.getUser(userId);

            if (user != null && user.getRole().equalsIgnoreCase(selectedRole)) {
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + user.getName() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();

                // --- ALL ROLES ARE NOW FUNCTIONAL ---
                switch (user.getRole().toLowerCase()) {
                    case "admin":
                        new AdminDashboard().setVisible(true);
                        break;
                    case "ngo":
                        new NgoDashboard(user).setVisible(true); // <-- Pass user
                        break;
                    case "volunteer":
                        new VolunteerDashboard(user).setVisible(true); // <-- Pass user
                        break;
                    case "user":
                        new UserDashboard(user).setVisible(true);
                        break;
                    default:
                        new UserDashboard(user).setVisible(true);
                        break;
                }

            } else {
                JOptionPane.showMessageDialog(this, "Invalid User ID or Role. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during login.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(30, 60, 110));
        return label;
    }

    private JTextField createTextField(Font font) {
        JTextField textField = new JTextField(15);
        textField.setFont(font);
        textField.setBorder(BorderFactory.createLineBorder(new Color(150, 180, 220), 1, true));
        return textField;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 2, true));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}