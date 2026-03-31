package com.rescueapp.db.dao;

import com.rescueapp.core.Report;
import com.rescueapp.core.User; // <-- Added missing import
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueAppReportDAO {
    private final Connection conn;

    public RescueAppReportDAO(RescueAppDbConnector conn1) {
        this.conn = conn1.getMySQLConnection();
    }

    
    public Report addReport(Report r) throws SQLException {
        String sql = "INSERT INTO reports (reportId, description, status, reporterId, date, photoUrl, location, urgency) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getReportId());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatus());
            ps.setString(4, r.getReporter() != null ? r.getReporter().getUserId() : null);
            ps.setDate(5, r.getDate() != null ? new java.sql.Date(r.getDate().getTime()) : null);
            ps.setString(6, r.getPhotoUrl());
            ps.setString(7, r.getLocation());
            ps.setString(8, r.getUrgency());
            ps.executeUpdate();
        }
        return r;
    }

    	public List<Report> getAllReportsWithDetails() throws SQLException {
        List<Report> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as reporterName " +
                     "FROM reports r LEFT JOIN users u ON r.reporterId = u.userId " +
                     "ORDER BY r.date DESC, r.reportId DESC"; // Show newest first

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Report r = new Report();
                r.setReportId(rs.getString("reportId"));
                r.setDescription(rs.getString("description"));
                r.setStatus(rs.getString("status"));
                r.setLocation(rs.getString("location"));
                r.setPhotoUrl(rs.getString("photoUrl"));
                r.setUrgency(rs.getString("urgency"));
                r.setDate(rs.getDate("date")); // Get as java.sql.Date

                String reporterId = rs.getString("reporterId");
                if (reporterId != null) {
                    User reporter = new User();
                    reporter.setUserId(reporterId);
                    reporter.setName(rs.getString("reporterName"));
                    r.setReporter(reporter);
                }
                list.add(r);
            }
        }
        return list;
    }

 // ... (inside RescueAppReportDAO class)

    // Method to get all reports (you likely have this already)
    public List<Report> getAllReports() throws SQLException {
        // ... (implementation) ...
        List<Report> list = new ArrayList<>();
        String sql = "SELECT r.*, u.name as reporterName FROM reports r LEFT JOIN users u ON r.reporterId = u.userId ORDER BY r.date DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
             while (rs.next()) {
                Report report = new Report();
                report.setReportId(rs.getString("reportId"));
                report.setDescription(rs.getString("description"));
                report.setStatus(rs.getString("status"));
                report.setLocation(rs.getString("location"));
                report.setPhotoUrl(rs.getString("photoUrl"));
                report.setUrgency(rs.getString("urgency"));
                Date date = rs.getDate("date");
                if (date != null) report.setDate(date);

                User reporter = new User();
                reporter.setUserId(rs.getString("reporterId"));
                reporter.setName(rs.getString("reporterName"));
                report.setReporter(reporter);

                list.add(report);
             }
        }
        return list;
    }
    public boolean updateReportStatus(String reportId, String newStatus) throws SQLException {
        String sql = "UPDATE reports SET status = ? WHERE reportId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, reportId);
            return ps.executeUpdate() > 0;
        }
    }

    // ... (rest of the DAO class)

    
    public String getReportById(String reportId) {
        String reportDetails = "";
        RescueAppDbConnector db = new RescueAppDbConnector();
        Connection localConn = db.getMySQLConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        if (localConn == null) return "Error: Could not connect to database.";

        try {
            String sql = "SELECT reportId, description, date, status, location, urgency FROM reports WHERE reportId = ?";
            pstmt = localConn.prepareStatement(sql);
            pstmt.setString(1, reportId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                reportDetails = String.format(
                    "\nReport Details:\n------------------\n" +
                    "Report ID: %s\nDescription: %s\nDate: %s\nStatus: %s\nLocation: %s\nUrgency: %s\n------------------",
                    rs.getString("reportId"), rs.getString("description"), rs.getString("date"),
                    rs.getString("status"), rs.getString("location"), rs.getString("urgency")
                );
            } else {
                reportDetails = "No report found for ID: " + reportId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportDetails = "Error fetching report: " + e.getMessage();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (localConn != null) localConn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return reportDetails;
    }
}