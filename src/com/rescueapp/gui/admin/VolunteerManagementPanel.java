package com.rescueapp.gui.admin;

import com.rescueapp.core.Volunteer;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppVolunteerDAO; // Use Volunteer DAO

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID; // For generating ID if needed

@SuppressWarnings("serial")
public class VolunteerManagementPanel extends JPanel {

    private RescueAppVolunteerDAO volunteerDAO;
    private DefaultTableModel model;
    private JTable table;

    // Fields for Add/Edit Dialog (similar to User panel, add 'availability')
    private JTextField idFieldDialog = new JTextField(20);
    private JTextField nameFieldDialog = new JTextField(20);
    private JTextField emailFieldDialog = new JTextField(20);
    private JTextField contactFieldDialog = new JTextField(20);
    private JTextField availabilityFieldDialog = new JTextField(20); // Volunteer specific

    public VolunteerManagementPanel() {
        super(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            volunteerDAO = new RescueAppVolunteerDAO(db); // Use Volunteer DAO
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to Volunteer DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        JLabel title = new JLabel("Manage Volunteers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Table - add 'Availability' column
        String[] columns = {"User ID", "Name", "Email", "Contact", "Availability"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add New Volunteer");
        JButton editButton = new JButton("Edit Selected Volunteer");
        JButton deleteButton = new JButton("Delete Selected Volunteer");

        addButton.addActionListener(e -> showVolunteerDialog(null));
        editButton.addActionListener(e -> showVolunteerDialog(getSelectedVolunteer()));
        deleteButton.addActionListener(e -> handleDeleteVolunteer());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadVolunteers();
    }

    private void loadVolunteers() {
        model.setRowCount(0);
        if (volunteerDAO == null) return;
        try {
            // Ensure getAllVolunteers returns List<Volunteer>
            List<Volunteer> volunteers = volunteerDAO.getAllVolunteers();
            for (Volunteer vol : volunteers) {
                model.addRow(new Object[]{
                        vol.getUserId(), vol.getName(), vol.getEmail(), vol.getContact(), vol.getAvailability()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Failed to load volunteers: " + e.getMessage(), "", "", ""});
        } catch (ClassCastException e) {
             e.printStackTrace();
             model.addRow(new Object[]{"Error", "DAO returned wrong type", "", "", ""});
             JOptionPane.showMessageDialog(this, "DAO Configuration Error: Expected Volunteer list.", "Type Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Volunteer getSelectedVolunteer() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a volunteer from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        // Assuming column order matches the table
        String userId = (String) model.getValueAt(selectedRow, 0);
        String name = (String) model.getValueAt(selectedRow, 1);
        String email = (String) model.getValueAt(selectedRow, 2);
        String contact = (String) model.getValueAt(selectedRow, 3);
        String availability = (String) model.getValueAt(selectedRow, 4);

        // Create a Volunteer object
        Volunteer vol = new Volunteer(userId, name, email, contact);
        vol.setAvailability(availability);
        return vol;
    }

    private void showVolunteerDialog(Volunteer volToEdit) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        boolean isEditMode = (volToEdit != null);
        if (isEditMode) {
            idFieldDialog.setText(volToEdit.getUserId());
            idFieldDialog.setEditable(false);
            nameFieldDialog.setText(volToEdit.getName());
            emailFieldDialog.setText(volToEdit.getEmail());
            contactFieldDialog.setText(volToEdit.getContact());
            availabilityFieldDialog.setText(volToEdit.getAvailability());
        } else {
            idFieldDialog.setText("[Auto-Generated]");
            idFieldDialog.setEditable(false);
            nameFieldDialog.setText("");
            emailFieldDialog.setText("");
            contactFieldDialog.setText("");
            availabilityFieldDialog.setText(""); // Default availability?
        }

        // Add fields to panel
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(idFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(nameFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(emailFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(contactFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Availability:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(availabilityFieldDialog, gbc);


        String title = isEditMode ? "Edit Volunteer" : "Add New Volunteer";
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameFieldDialog.getText().trim();
            String email = emailFieldDialog.getText().trim();
            String contact = contactFieldDialog.getText().trim();
            String availability = availabilityFieldDialog.getText().trim();

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Email, and Contact are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Volunteer volunteer = isEditMode ? volToEdit : new Volunteer();
            volunteer.setName(name);
            volunteer.setEmail(email);
            volunteer.setContact(contact);
            volunteer.setAvailability(availability);
            // Role is implicitly "Volunteer"

            try {
                if (isEditMode) {
                    volunteerDAO.updateVolunteer(volunteer);
                    JOptionPane.showMessageDialog(this, "Volunteer updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    volunteer.setUserId(UUID.randomUUID().toString()); // Generate ID
                    volunteerDAO.addVolunteer(volunteer);
                    JOptionPane.showMessageDialog(this, "Volunteer added successfully with ID: " + volunteer.getUserId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadVolunteers(); // Refresh
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteVolunteer() {
         Volunteer volToDelete = getSelectedVolunteer();
        if (volToDelete == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete volunteer '" + volToDelete.getName() + "' (ID: " + volToDelete.getUserId() + ")?\nThis might affect assigned tasks.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = volunteerDAO.deleteVolunteer(volToDelete.getUserId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Volunteer deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadVolunteers();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete volunteer.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Database error during deletion: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}