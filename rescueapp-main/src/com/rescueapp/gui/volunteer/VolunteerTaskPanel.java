package com.rescueapp.gui.volunteer;

import com.rescueapp.core.Task;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppTaskDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class VolunteerTaskPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppTaskDAO taskDAO;
    private User loggedInVolunteer;

    public VolunteerTaskPanel(User user) {
        super(new BorderLayout(10, 10));
        this.loggedInVolunteer = user;

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            taskDAO = new RescueAppTaskDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("My Assigned Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Task ID", "Description", "Status", "Date"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Buttons to update status ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton startButton = new JButton("Start Task");
        startButton.addActionListener(e -> updateTaskStatus("In Progress"));
        
        JButton completeButton = new JButton("Complete Task");
        completeButton.addActionListener(e -> updateTaskStatus("Completed"));
        
        bottomPanel.add(startButton);
        bottomPanel.add(completeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTasks();
    }

    private void loadTasks() {
        model.setRowCount(0);
        if (taskDAO == null) return;
        try {
            // Use the new method from your DAO
            List<Task> tasks = taskDAO.getTasksForVolunteer(loggedInVolunteer.getUserId());
            for (Task task : tasks) {
                model.addRow(new Object[]{
                        task.getTaskId(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDate()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTaskStatus(String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to update.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String taskId = (String) model.getValueAt(selectedRow, 0);
        try {
            taskDAO.updateTaskStatus(taskId, newStatus);
            JOptionPane.showMessageDialog(this, "Task status updated to '" + newStatus + "'!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTasks(); // Refresh the table
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update task status.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}