package com.rescueapp.gui.volunteer;

import com.rescueapp.core.Report;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppReportDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class VolunteerReportPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppReportDAO reportDAO;

    public VolunteerReportPanel(User user) {
        super(new BorderLayout(10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            reportDAO = new RescueAppReportDAO(db);
        } catch (Exception e) { e.printStackTrace(); }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("View & Update Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Report ID", "Description", "Location", "Urgency", "Status", "Date"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.getColumnModel().getColumn(0).setMinWidth(0); // Hide ID
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(350); // Description
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // Location

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Buttons to update status ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton investigatingButton = new JButton("Mark as Investigating");
        investigatingButton.addActionListener(e -> updateReportStatus("Investigating"));
        
        JButton resolvedButton = new JButton("Mark as Resolved");
        resolvedButton.addActionListener(e -> updateReportStatus("Resolved"));
        
        bottomPanel.add(investigatingButton);
        bottomPanel.add(resolvedButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadReports();
    }

    private void loadReports() {
        model.setRowCount(0);
        if (reportDAO == null) return;
        try {
            List<Report> reports = reportDAO.getAllReports();
            for (Report report : reports) {
                // Show only reports that are 'Open' or 'Investigating' for volunteers? (Optional filter)
                // if (report.getStatus().equalsIgnoreCase("Open") || report.getStatus().equalsIgnoreCase("Investigating")) {
                    model.addRow(new Object[]{
                            report.getReportId(),
                            report.getDescription(),
                            report.getLocation(),
                            report.getUrgency(),
                            report.getStatus(),
                            report.getDate()
                    });
                // }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateReportStatus(String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a report to update.", "No Report Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportId = (String) model.getValueAt(selectedRow, 0);
        try {
            reportDAO.updateReportStatus(reportId, newStatus);
            JOptionPane.showMessageDialog(this, "Report status updated to '" + newStatus + "'!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadReports(); // Refresh
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update report status.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}