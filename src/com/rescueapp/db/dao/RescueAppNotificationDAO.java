package com.rescueapp.db.dao;

import com.rescueapp.core.Notification;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Import for new method

public class RescueAppNotificationDAO {
    private Connection conn;

    public RescueAppNotificationDAO(RescueAppDbConnector conn) {
        this.conn = conn.getMySQLConnection();
    }

    /**
     * NEW, CONVENIENCE METHOD
     * Adds a notification using just the userId and message.
     */
    public boolean addNotification(String userId, String message) throws SQLException {
        String notifId = UUID.randomUUID().toString();
        
        String sql = "INSERT INTO notifications (notificationId, receiverId, message, date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notifId);
            ps.setString(2, userId);
            ps.setString(3, message);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Current time
            ps.setString(5, "Unread");
            
            return ps.executeUpdate() > 0;
        }
    }
    
    /**
     * Original method (also corrected to use your table structure)
     * Adds a notification from a full Notification object.
     */
    public boolean addNotification(Notification n) throws SQLException {
        String sql = "INSERT INTO notifications (notificationId, receiverId, message, date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String recipientId = null;
            if (n.getRecipient() instanceof User) {
                recipientId = ((User) n.getRecipient()).getUserId();
            }
            if (recipientId == null) {
                if (n.getRecipient() != null) {
                    try {
                        recipientId = ((User) n.getRecipient()).getUserId();
                    } catch (ClassCastException e) {
                         throw new SQLException("Recipient must be a User object with a valid ID.");
                    }
                } else {
                     throw new SQLException("Recipient cannot be null.");
                }
            }

            ps.setString(1, n.getNotificationId());
            ps.setString(2, recipientId);
            ps.setString(3, n.getMessage());
            ps.setTimestamp(4, Timestamp.from(n.getCreatedAt())); // Matches 'date' (datetime) column
            ps.setString(5, n.isRead() ? "Read" : "Unread"); // Matches 'status' column
            
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Fetches all notifications for a specific user.
     * (Corrected to use 'receiverId', 'status', and 'date')
     */
    public List<Notification> getNotificationsForUser(String userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE receiverId = ? ORDER BY status DESC, date DESC"; 
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String notifId = rs.getString("notificationId");
                    String message = rs.getString("message");
                    Timestamp ts = rs.getTimestamp("date"); // Use 'date' column
                    String status = rs.getString("status"); // Use 'status' column
                    
                    Notification n = new Notification(notifId, message, null); 
                    
                    boolean isRead = (status != null && status.equalsIgnoreCase("Read"));
                    if (isRead) {
                        n.markAsRead();
                    }
                    
                    list.add(n);
                }
            }
        }
        return list;
    }

    /**
     * Marks a specific notification as read.
     * (Corrected to use 'status' column)
     */
    public boolean markAsRead(String notificationId) throws SQLException {
        String sql = "UPDATE notifications SET status = 'Read' WHERE notificationId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, notificationId);
            return ps.executeUpdate() > 0;
        }
    }
}