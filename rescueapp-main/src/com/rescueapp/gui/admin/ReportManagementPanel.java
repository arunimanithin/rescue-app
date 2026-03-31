package com.rescueapp.gui.admin;

import com.rescueapp.core.Report;
import java.awt.event.ActionEvent;
import com.rescueapp.core.User; // To display reporter info
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppReportDAO;
import com.rescueapp.db.dao.RescueAppUserDAO; // To fetch user details if needed

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

@SuppressWarnings("serial")
public class ReportManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppReportDAO reportDAO;
    
    private JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Open", "In Progress", "Resolved"});
    private JButton updateStatusButton = new JButton("Update Status");

    public ReportManagementPanel() {
        super(new BorderLayout(10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            reportDAO = new RescueAppReportDAO(db);
            // userDAO = new RescueAppUserDAO(db); // If fetching user names
        } catch (Exception e) {
            // Handle DB error
             e.printStackTrace();
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Manage Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Report ID", "Description", "Location", "Urgency", "Date", "Reporter ID", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        // ... (Table setup) ...
         table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.add(new JLabel("Set Status:"));
        controlPanel.add(statusComboBox);
        controlPanel.add(updateStatusButton);
        
        add(controlPanel, BorderLayout.SOUTH);

        // --- Button Action ---
        updateStatusButton.addActionListener(this::handleUpdateStatus);

        // Load data
        loadAllReports();
    }

    private void loadAllReports() {
        model.setRowCount(0);
        if (reportDAO == null) return;
        try {
           List<Report> reports = reportDAO.getAllReportsWithDetails(); // ** Needs DAO Update **
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Report r : reports) {
                String reporterId = r.getReporter() != null ? r.getReporter().getUserId() : "N/A";
                String reportDate = r.getDate() != null ? dateFormat.format(r.getDate()) : "N/A";
                model.addRow(new Object[]{
                        r.getReportId(), r.getDescription(), r.getLocation(),
                        r.getUrgency(), reportDate, reporterId, r.getStatus()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle error
            model.addRow(new Object[]{"Error", "Could not load reports", "", "", "", "", ""});
        }
    }

     private void handleUpdateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String reportId = (String) model.getValueAt(selectedRow, 0);
        String newStatus = (String) statusComboBox.getSelectedItem();

         try {
            boolean success = reportDAO.updateStatus(reportId, newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Report status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAllReports(); // Refresh
            } else { /* Error message */ }
        } catch (SQLException ex) { /* Error message */ }
    }

	 private void handleUpdateStatus(ActionEvent actionevent1) {
	 }

  
}