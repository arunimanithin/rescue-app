package com.rescueapp.gui.admin;

import com.rescueapp.core.NGO;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppNGODAO; // Use NGO DAO
import com.rescueapp.db.dao.RescueAppUserDAO; // To manage base user record

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("serial")
public class NgoManagementPanel extends JPanel {

    private RescueAppNGODAO ngoDAO;
    private RescueAppUserDAO userDAO; // For base user management
    private DefaultTableModel model;
    private JTable table;

    // Fields for Add/Edit Dialog
    private JTextField idFieldDialog = new JTextField(20);
    private JTextField nameFieldDialog = new JTextField(20); // Corresponds to organizationName too
    private JTextField emailFieldDialog = new JTextField(20);
    private JTextField contactFieldDialog = new JTextField(20);

    public NgoManagementPanel() {
        super(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            ngoDAO = new RescueAppNGODAO(db);
            userDAO = new RescueAppUserDAO(db); // Initialize UserDAO
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to DBs.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        JLabel title = new JLabel("Manage NGOs");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        
        String[] columns = {"User ID", "Organization Name", "Email", "Contact"};
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
        JButton addButton = new JButton("Add New NGO");
        JButton editButton = new JButton("Edit Selected NGO");
        JButton deleteButton = new JButton("Delete Selected NGO");

        addButton.addActionListener(e -> showNgoDialog(null));
        editButton.addActionListener(e -> showNgoDialog(getSelectedNgo()));
        deleteButton.addActionListener(e -> handleDeleteNgo());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadNgos();
    }

    private void loadNgos() {
        model.setRowCount(0);
        if (ngoDAO == null) return;
        try {
            List<NGO> ngos = ngoDAO.getAllNgos();
            for (NGO ngo : ngos) {
                // Fetching OrganizationName might require DAO change or assume Name is Org Name
                model.addRow(new Object[]{
                        ngo.getUserId(), ngo.getName(), ngo.getEmail(), ngo.getContact()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Failed to load NGOs: " + e.getMessage(), "", ""});
        }
    }

    private NGO getSelectedNgo() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an NGO.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String userId = (String) model.getValueAt(selectedRow, 0);
        String name = (String) model.getValueAt(selectedRow, 1);
        String email = (String) model.getValueAt(selectedRow, 2);
        String contact = (String) model.getValueAt(selectedRow, 3);
        // Using basic constructor
        return new NGO(userId, name, email, contact);
    }

     private void showNgoDialog(NGO ngoToEdit) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;


        boolean isEditMode = (ngoToEdit != null);
        if (isEditMode) {
            idFieldDialog.setText(ngoToEdit.getUserId());
            idFieldDialog.setEditable(false);
            nameFieldDialog.setText(ngoToEdit.getName()); // Use getName() as default Org Name
            emailFieldDialog.setText(ngoToEdit.getEmail());
            contactFieldDialog.setText(ngoToEdit.getContact());
        } else {
             idFieldDialog.setText("[Auto-Generated]");
             idFieldDialog.setEditable(false);
            nameFieldDialog.setText("");
            emailFieldDialog.setText("");
            contactFieldDialog.setText("");
        }

        // Add fields to panel
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(idFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Org Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(nameFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(emailFieldDialog, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(contactFieldDialog, gbc);

        String title = isEditMode ? "Edit NGO" : "Add New NGO";
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameFieldDialog.getText().trim();
            String email = emailFieldDialog.getText().trim();
            String contact = contactFieldDialog.getText().trim();

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Name, Email, and Contact are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            NGO ngo = isEditMode ? ngoToEdit : new NGO();
            ngo.setName(name);
            ngo.setEmail(email);
            ngo.setContact(contact);
            ngo.setRole("NGO"); // Explicitly set role
            // Optionally set organization name if different from name
            ngo.setOrganizationName(name);

            try {
                if (isEditMode) {
                    
                    userDAO.updateUser(ngo); // Assumes updateUser handles name, email, contact
                    JOptionPane.showMessageDialog(this, "NGO updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    ngo.setUserId(UUID.randomUUID().toString());
                    
                    userDAO.addUser(ngo);
                   
                    ngoDAO.addNGO(ngo);
                    JOptionPane.showMessageDialog(this, "NGO added successfully with ID: " + ngo.getUserId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadNgos(); // Refresh
            } catch (SQLException ex) {
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteNgo() {
        NGO ngoToDelete = getSelectedNgo();
        if (ngoToDelete == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this, "Delete NGO '" + ngoToDelete.getName() + "' (ID: " + ngoToDelete.getUserId() + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Delete from ngos table first
                boolean ngoSuccess = ngoDAO.deleteUser(ngoToDelete.getUserId()); // Method name might be deleteNgo
                 if (ngoSuccess) {
                    // Then delete from users table
                     userDAO.deleteUser(ngoToDelete.getUserId());
                    JOptionPane.showMessageDialog(this, "NGO deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadNgos();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete NGO record.", "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                 ex.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Database error during deletion: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}