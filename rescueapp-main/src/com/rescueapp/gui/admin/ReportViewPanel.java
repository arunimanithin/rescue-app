package com.rescueapp.gui.admin;

import com.rescueapp.core.Report;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppReportDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// View-only version of Report Management
@SuppressWarnings("serial")
public class ReportViewPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppReportDAO reportDAO;

    public ReportViewPanel() {
        super(new BorderLayout(10, 10));
        
        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            reportDAO = new RescueAppReportDAO(db);
        } catch (Exception e) { e.printStackTrace(); }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("View User Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Description", "Location", "Urgency", "Status", "Date"}; // No IDs
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(350); // Description
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Location

        add(new JScrollPane(table), BorderLayout.CENTER);
        loadReports();
    }

    private void loadReports() {
        model.setRowCount(0);
        if (reportDAO == null) return;
        try {
            List<Report> reports = reportDAO.getAllReports();
            for (Report report : reports) {
                model.addRow(new Object[]{
                        report.getDescription(), report.getLocation(),
                        report.getUrgency(), report.getStatus(), report.getDate()
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}