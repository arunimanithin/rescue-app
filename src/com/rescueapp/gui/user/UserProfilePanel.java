package com.rescueapp.gui.user;

import com.rescueapp.core.User;
import com.rescueapp.core.Volunteer; // Import Volunteer
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppUserDAO;
import com.rescueapp.db.dao.RescueAppVolunteerDAO; // Import Volunteer DAO

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class UserProfilePanel extends JPanel {

    // Fields for user info
    private JTextField nameField;
    private JTextField emailField;
    private JTextField contactField;
    private JTextField availabilityField; 
    private JLabel availabilityLabel;     
    private JButton updateButton;

    private User loggedInUser;
    private RescueAppUserDAO userDAO;
    private RescueAppVolunteerDAO volunteerDAO; 

    public UserProfilePanel(User user) {
        super(new GridBagLayout());
        this.loggedInUser = user;

        // Initialize DAOs
        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            userDAO = new RescueAppUserDAO(db);
            // Only initialize volunteerDAO if the user is a Volunteer
            if (loggedInUser instanceof Volunteer) {
                volunteerDAO = new RescueAppVolunteerDAO(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        // --- Panel Setup ---
        setBackground(new Color(245, 245, 245)); // Whitesmoke
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = setupGridBagConstraints();

        // --- Title ---
        addTitleLabel(gbc);

        // --- Read-only fields ---
        addReadOnlyField("User ID:", loggedInUser.getUserId(), gbc, 1);
        addReadOnlyField("Role:", loggedInUser.getRole(), gbc, 2);

        // --- Editable fields ---
        nameField = addEditableField("Name:", loggedInUser.getName(), gbc, 3);
        emailField = addEditableField("Email:", loggedInUser.getEmail(), gbc, 4);
        contactField = addEditableField("Contact:", loggedInUser.getContact(), gbc, 5);

        // --- Volunteer-specific field ---
        addVolunteerFields(gbc, 6);

        // --- Update Button ---
        addUpdateButton(gbc, 7);
    }

    

    private GridBagConstraints setupGridBagConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addTitleLabel(GridBagConstraints gbc) {
        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(title, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; // Reset
    }

    private void addReadOnlyField(String labelText, String value, GridBagConstraints gbc, int gridy) {
        gbc.gridx = 0; gbc.gridy = gridy;
        add(createLabel(labelText), gbc);
        JTextField field = new JTextField(value, 20);
        field.setEditable(false);
        field.setBackground(Color.LIGHT_GRAY);
        gbc.gridx = 1; add(field, gbc);
    }

    private JTextField addEditableField(String labelText, String value, GridBagConstraints gbc, int gridy) {
        gbc.gridx = 0; gbc.gridy = gridy;
        add(createLabel(labelText), gbc);
        JTextField field = new JTextField(value, 20);
        gbc.gridx = 1; add(field, gbc);
        return field;
    }

    private void addVolunteerFields(GridBagConstraints gbc, int gridy) {
        availabilityLabel = createLabel("Availability:");
        availabilityField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = gridy;
        add(availabilityLabel, gbc);
        gbc.gridx = 1; add(availabilityField, gbc);

        // Only show and populate if the user is a Volunteer
        if (loggedInUser instanceof Volunteer) {
            availabilityLabel.setVisible(true);
            availabilityField.setVisible(true);
            availabilityField.setText(((Volunteer) loggedInUser).getAvailability());
        } else {
            availabilityLabel.setVisible(false);
            availabilityField.setVisible(false);
        }
    }

     private void addUpdateButton(GridBagConstraints gbc, int gridy) {
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        updateButton = new JButton("Update My Information");
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        updateButton.setBackground(new Color(30, 144, 255)); // Dodger Blue
        updateButton.setForeground(Color.WHITE);
        updateButton.addActionListener(this::handleUpdateProfile);
        add(updateButton, gbc);
    }

    // --- Action Handler ---
    private void handleUpdateProfile(ActionEvent e) {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newContact = contactField.getText().trim();
        String newAvailability = availabilityField.getText().trim(); // Get availability

        // Basic validation
        if (newName.isEmpty() || newEmail.isEmpty() || newContact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email, and Contact cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Update the user object (common fields)
        loggedInUser.setName(newName);
        loggedInUser.setEmail(newEmail);
        loggedInUser.setContact(newContact);

        // Update volunteer-specific field if applicable
        if (loggedInUser instanceof Volunteer) {
            ((Volunteer) loggedInUser).setAvailability(newAvailability);
        }

        
        try {
            // Update common user details first
            boolean userSuccess = userDAO.updateUser(loggedInUser);

            boolean volunteerSuccess = true; // Assume success if not a volunteer
            // If volunteer, update volunteer-specific details
            if (loggedInUser instanceof Volunteer && volunteerDAO != null) {
                volunteerSuccess = volunteerDAO.updateVolunteer((Volunteer) loggedInUser);
            }

            if (userSuccess && volunteerSuccess) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else if (!userSuccess) {
                 JOptionPane.showMessageDialog(this, "Could not update general profile info. User not found?", "Error", JOptionPane.ERROR_MESSAGE);
            } else { // !volunteerSuccess
                 JOptionPane.showMessageDialog(this, "Could not update volunteer-specific info.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update profile due to a database error.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }
}