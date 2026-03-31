package com.rescueapp.gui.admin;

import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppUserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class UserManagementPanel extends JPanel {

    private RescueAppUserDAO userDAO;
    private DefaultTableModel model;
    private JTable table;

    public UserManagementPanel() {
        super(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            userDAO = new RescueAppUserDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to User DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        // Title
        JLabel title = new JLabel("Manage All Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // --- 1. TABLE MODEL (Unchanged) ---
        String[] columns = {"User ID", "Name", "Email", "Role", "Contact"};
        model = new DefaultTableModel(columns, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);

        // --- 2. HIDE THE 'User ID' COLUMN ---
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // --- 3. SET PREFERRED COLUMN SIZES ---
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(250); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Role
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Contact

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form for adding/editing
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.add(new JButton("Add New User"));
        formPanel.add(new JButton("Edit Selected User"));
        formPanel.add(new JButton("Delete Selected User"));
        add(formPanel, BorderLayout.SOUTH);

        // Load data from the database
        loadUsers();
    }

    private void loadUsers() {
        model.setRowCount(0);
        if (userDAO == null) {
            model.addRow(new Object[]{"Error", "Could not connect to database", "", "", ""});
            return;
        }

        try {
            List<User> users = userDAO.getAllUsers();
            for (User user : users) {
                // --- 4. ROW DATA (Unchanged) ---
                model.addRow(new Object[]{
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getContact()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Failed to load users: " + e.getMessage(), "", "", ""});
        }
    }
}