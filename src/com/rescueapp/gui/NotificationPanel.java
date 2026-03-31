package com.rescueapp.gui;

import com.rescueapp.core.Notification;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppNotificationDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class NotificationPanel extends JPanel {

    private JList<Notification> notificationList;
    private DefaultListModel<Notification> listModel;
    private RescueAppNotificationDAO notificationDAO;
    private User loggedInUser;
    private Map<Notification, String> notificationIdMap; // Maps list object to its ID

    public NotificationPanel(User user) {
        super(new BorderLayout(10, 10));
        this.loggedInUser = user;
        this.notificationIdMap = new HashMap<>();

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            notificationDAO = new RescueAppNotificationDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to Notification DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("My Notifications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        notificationList = new JList<>(listModel);
        notificationList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationList.setCellRenderer(new NotificationCellRenderer());
        add(new JScrollPane(notificationList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markAsReadButton = new JButton("Mark Selected as Read");
        markAsReadButton.addActionListener(e -> markAsRead());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadNotifications());
        bottomPanel.add(markAsReadButton);
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Auto-refresh when tab is shown
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println("Notification tab shown, refreshing...");
                loadNotifications();
            }
        });

        loadNotifications(); // Initial load
    }

    private void loadNotifications() {
        System.out.println("NotificationPanel: Loading notifications for user: " + (loggedInUser != null ? loggedInUser.getUserId() : "null"));
        listModel.clear();
        notificationIdMap.clear();
        if (notificationDAO == null) {
             System.out.println("NotificationPanel: DAO is null, cannot load.");
             listModel.addElement(new Notification("","Error: DB Connection Failed.", null));
            return;
        }

        try {
            List<Notification> notifications = notificationDAO.getNotificationsForUser(loggedInUser.getUserId());
            System.out.println("NotificationPanel: Fetched " + notifications.size() + " notifications from DB.");
            if(notifications.isEmpty()){
                // Create a dummy, non-functional notification object to display the message
                Notification noNotif = new Notification("","No new notifications.", null);
                noNotif.markAsRead(); // Mark it so it appears greyed out
                listModel.addElement(noNotif);
            } else {
                for (Notification n : notifications) {
                    System.out.println("  - Adding notification to list: " + n.getMessage() + " (Read: " + n.isRead() + ")");
                    listModel.addElement(n);
                    notificationIdMap.put(n, n.getNotificationId()); // Store ID from the object
                }
            }
        } catch (SQLException e) {
            System.err.println("!!! NotificationPanel: SQL Error loading notifications: " + e.getMessage());
            e.printStackTrace();
            listModel.addElement(new Notification("","Error loading notifications.", null));
        } catch (Exception e) {
             System.err.println("!!! NotificationPanel: Unexpected Error loading notifications: " + e.getMessage());
             e.printStackTrace();
             listModel.addElement(new Notification("","Error loading notifications.", null));
        }
    }

    private void markAsRead() {
        Notification selected = notificationList.getSelectedValue();
        if (selected == null || selected.getNotificationId() == null || selected.getNotificationId().isEmpty()) { // Check if it's the dummy message
            JOptionPane.showMessageDialog(this, "Please select a valid notification.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.isRead()) {
            JOptionPane.showMessageDialog(this, "This notification is already marked as read.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String notificationId = notificationIdMap.get(selected);
        if (notificationId == null) {
             System.err.println("Error: Could not find notification ID in map for selected item.");
            return;
        }

        try {
            boolean success = notificationDAO.markAsRead(notificationId);
            if (success) {
                selected.markAsRead();
                notificationList.repaint(); // Refresh the specific item visually
                JOptionPane.showMessageDialog(this, "Notification marked as read.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to update notification status in DB.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update notification.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Inner class for custom list rendering ---
    private static class NotificationCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Notification) {
                Notification n = (Notification) value;
                String text = n.getMessage();
                if (n.isRead()) {
                    setText("<html><i style='color:gray;'>" + text + "</i></html>"); // Removed "(Read)" for cleaner look
                } else {
                    setText("<html><b>" + text + "</b></html>");
                }
            } else {
                 setText(value.toString()); // Fallback for non-Notification objects
            }
            return c;
        }
    }
}