package com.rescueapp.gui.admin;

import com.rescueapp.core.Task;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppTaskDAO;
import com.rescueapp.db.dao.RescueAppUserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent; // Required for event listener
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

@SuppressWarnings("serial")
public class TaskManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppTaskDAO taskDAO;
    private RescueAppUserDAO userDAO;

    // Form fields
    private JTextArea descriptionArea = new JTextArea(3, 20);
    private JComboBox<UserComboBoxItem> assigneeComboBox;
    private JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Open", "In Progress", "Done"});
    private JButton assignButton = new JButton("Assign New Task");
    private JButton updateStatusButton = new JButton("Update Status");

    public TaskManagementPanel() {
        super(new BorderLayout(10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            // NOTE: Ensure your RescueAppTaskDAO package is correctly linked in your IDE (It was previously com_rescueapp.src.com.rescueapp.db.dao)
            // Assuming the simple name RescueAppTaskDAO works now.
            taskDAO = new RescueAppTaskDAO(db);
            userDAO = new RescueAppUserDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to Task DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Manage Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columns = {"Task ID", "Description", "Assignee", "Date Assigned", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // --- Form/Control Panel ---
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.EAST);

        // --- Button Actions ---
        assignButton.addActionListener(this::handleAssignTask);
        updateStatusButton.addActionListener(this::handleUpdateStatus);

        // Load initial data
        loadAllTasks();
        populateAssigneeComboBox();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Task Actions"));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);

        // Description
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(createLabel("Description:", labelFont), gbc);
        descriptionArea.setFont(fieldFont);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        panel.add(new JScrollPane(descriptionArea), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;

        // Assignee
        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("Assign To:", labelFont), gbc);
        assigneeComboBox = new JComboBox<>(); // Populated later
        assigneeComboBox.setFont(fieldFont);
        gbc.gridx = 1; panel.add(assigneeComboBox, gbc);

        // Assign Button
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        assignButton.setFont(fieldFont);
        panel.add(assignButton, gbc);

        // --- Separator ---
        gbc.gridy++; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(15, 0, 15, 0);
        panel.add(new JSeparator(), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 5, 5, 5); // Reset

        // Update Status Section
        gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(createLabel("Set Status:", labelFont), gbc);
        statusComboBox.setFont(fieldFont);
        gbc.gridx = 1; panel.add(statusComboBox, gbc);

        // Update Button
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        updateStatusButton.setFont(fieldFont);
        panel.add(updateStatusButton, gbc);

        return panel;
    }

    private void loadAllTasks() {
        model.setRowCount(0);
        if (taskDAO == null) return;
        try {
            // Ensure getAllTasks method returns tasks with assignee details
            List<Task> tasks = taskDAO.getAllTasks(); 
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (Task task : tasks) {
                 String assigneeName = task.getAssignee() != null ? task.getAssignee().getName() : "Unassigned/Deleted";
                 String formattedDate = task.getDate() != null ? dateFormat.format(task.getDate()) : "N/A";
                model.addRow(new Object[]{
                        task.getTaskId(), task.getDescription(), assigneeName, formattedDate, task.getStatus()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateAssigneeComboBox() {
        if (userDAO == null) return;
        try {
            List<User> users = userDAO.getAllUsers();
            Vector<UserComboBoxItem> comboBoxItems = new Vector<>();
            comboBoxItems.add(new UserComboBoxItem(null, "[Unassigned]"));
            for (User user : users) {
                if ("Volunteer".equalsIgnoreCase(user.getRole()) || "NGO".equalsIgnoreCase(user.getRole())) {
                    comboBoxItems.add(new UserComboBoxItem(user, user.getName() + " (" + user.getRole() + ")"));
                }
            }
            assigneeComboBox.setModel(new DefaultComboBoxModel<>(comboBoxItems));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAssignTask(ActionEvent e) {
        String description = descriptionArea.getText().trim();
        UserComboBoxItem selectedItem = (UserComboBoxItem) assigneeComboBox.getSelectedItem();
        User assignee = (selectedItem != null) ? selectedItem.getUser() : null;

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task newTask = new Task();
        newTask.setTaskId(UUID.randomUUID().toString());
        newTask.setDescription(description);
        newTask.setAssignee(assignee);
        newTask.setDate(new Date());
        newTask.setStatus("Open");

        try {
            taskDAO.addTask(newTask); 
            JOptionPane.showMessageDialog(this, "Task assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAllTasks();
            descriptionArea.setText("");
            assigneeComboBox.setSelectedIndex(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to assign task.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void handleUpdateStatus(ActionEvent e) {
        
        int selectedRow = table.getSelectedRow();
       

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        
        String taskId = (String) model.getValueAt(selectedRow, 0);
        String newStatus = (String) statusComboBox.getSelectedItem();

         try {
            boolean success = taskDAO.updateTaskStatus(taskId, newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Task status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAllTasks(); // Refresh
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error updating status.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    
    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    // Wrapper class for User objects in JComboBox
    private static class UserComboBoxItem {
        private User user;
        private String display;

        public UserComboBoxItem(User user, String display) {
            this.user = user;
            this.display = display;
        }
        public User getUser() { return user; }
        @Override public String toString() { return display; }
    }
}