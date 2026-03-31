package com.rescueapp.gui.ngo;

import com.rescueapp.core.Report;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppReportDAO;
import com.rescueapp.db.dao.RescueAppTaskDAO;
import com.rescueapp.db.dao.RescueAppUserDAO;
import com.rescueapp.core.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Date;

// Very similar to Admin Report Panel
@SuppressWarnings("serial")
public class NgoReportPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppReportDAO reportDAO;
    private RescueAppTaskDAO taskDAO;
    private RescueAppUserDAO userDAO;

    public NgoReportPanel() {
        super(new BorderLayout(10, 10));
        
        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            reportDAO = new RescueAppReportDAO(db);
            taskDAO = new RescueAppTaskDAO(db);
            userDAO = new RescueAppUserDAO(db);
        } catch (Exception e) { e.printStackTrace(); }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("View User Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Report ID", "Description", "Location", "Urgency", "Status", "Date", "Reporter ID"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        // ... (Table setup - same as Admin Report panel) ...
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);

        add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createTaskButton = new JButton("Create Task From Report");
        createTaskButton.addActionListener(e -> createTaskFromReport());
        bottomPanel.add(createTaskButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadReports();
    }

    private void loadReports() {
        model.setRowCount(0);
        if (reportDAO == null) return;
        try {
            List<Report> reports = reportDAO.getAllReports();
            for (Report report : reports) {
                model.addRow(new Object[]{
                        report.getReportId(), report.getDescription(), report.getLocation(),
                        report.getUrgency(), report.getStatus(), report.getDate(),
                        (report.getReporter() != null) ? report.getReporter().getUserId() : "N/A"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // Identical to Admin's createTaskFromReport
    private void createTaskFromReport() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { /* ... show error ... */ return; }
        
        String description = (String) model.getValueAt(selectedRow, 1);
        String location = (String) model.getValueAt(selectedRow, 2);
        
        User assignee = null;
        try {
            assignee = userDAO.getAllUsers().stream()
                .filter(u -> u.getRole().equalsIgnoreCase("Volunteer"))
                .findFirst().orElse(null);
        } catch (SQLException e) { e.printStackTrace(); }

        if (assignee == null) { /* ... show error ... */ return; }

        com.rescueapp.core.Task task = new com.rescueapp.core.Task();
        task.setTaskId(java.util.UUID.randomUUID().toString());
        task.setDescription("Task from report: " + description + " at " + location);
        task.setStatus("Open");
        task.setDate(new Date());
        task.setAssignee(assignee);

        try {
            taskDAO.addTask(task);
            JOptionPane.showMessageDialog(this, "Task created and assigned!", "Success", JOptionPane.INFORMATION_MESSAGE);
            String reportId = (String) model.getValueAt(selectedRow, 0);
            reportDAO.updateReportStatus(reportId, "Assigned");
            loadReports();
        } catch (SQLException e) { /* ... show error ... */ }
    }
}