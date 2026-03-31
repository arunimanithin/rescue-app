package com.rescueapp.gui.admin;

import com.rescueapp.core.AdoptionRequest;
import com.rescueapp.core.Notification; // Ensure this is imported
import com.rescueapp.core.User;         // Ensure this is imported
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppAdoptionDAO;
import com.rescueapp.db.dao.RescueAppAnimalDAO;
import com.rescueapp.db.dao.RescueAppNotificationDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID; // Ensure this is imported
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class AdoptionRequestPanel extends JPanel { // Ensure 'extends JPanel' is correct

    private JTable table;
    private DefaultTableModel model;
    private RescueAppAdoptionDAO adoptionDAO;
    private RescueAppAnimalDAO animalDAO;
    private RescueAppNotificationDAO notificationDAO;
    private final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();

    // Default constructor for view-only (used by Admin)
    public AdoptionRequestPanel() {
        this(false); // Call the main constructor, hiding buttons
    }

    // Main constructor (used by NGO, potentially)
    public AdoptionRequestPanel(boolean showButtons) {
        super(new BorderLayout(10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            adoptionDAO = new RescueAppAdoptionDAO(db);
            animalDAO = new RescueAppAnimalDAO(db);
            notificationDAO = new RescueAppNotificationDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Adoption Requests"); // Simplified title
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Animal ID", "User ID", "User Name", "Request Date", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Hide IDs
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(2).setMinWidth(0);
        table.getColumnModel().getColumn(2).setMaxWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Add buttons only if requested
        if (showButtons) {
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton approveButton = new JButton("Approve Selected");
            approveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            approveButton.setBackground(new Color(40, 180, 90));
            approveButton.setForeground(Color.WHITE);

            JButton rejectButton = new JButton("Reject Selected");
            rejectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            rejectButton.setBackground(new Color(220, 50, 50));
            rejectButton.setForeground(Color.WHITE);

            bottomPanel.add(approveButton);
            bottomPanel.add(rejectButton);
            add(bottomPanel, BorderLayout.SOUTH);

            approveButton.addActionListener(e -> processRequest(true));
            rejectButton.addActionListener(e -> processRequest(false));
        }

        loadAllAdoptionRequests();
    }

    public void shutdownExecutor() {
        if (notificationExecutor != null && !notificationExecutor.isShutdown()) {
             System.out.println("Shutting down notification executor...");
            notificationExecutor.shutdown();
             try {
                 if (!notificationExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                     notificationExecutor.shutdownNow();
                 }
             } catch (InterruptedException e) {
                 notificationExecutor.shutdownNow();
                 Thread.currentThread().interrupt(); // Re-interrupt thread
             }
        }
    }

    private void loadAllAdoptionRequests() {
        model.setRowCount(0);
        if (adoptionDAO == null) { /* Handle error */ return; }
        try {
            // Using getAllRequests which returns AdoptionRequest objects
            List<AdoptionRequest> requests = adoptionDAO.getAllRequests();
            for (AdoptionRequest req : requests) {
                String adopterName = (req.getAdopter() != null && req.getAdopter().getName() != null) ? req.getAdopter().getName() : "N/A";
                String adopterId = (req.getAdopter() != null) ? req.getAdopter().getUserId() : "N/A";
                String animalId = (req.getAnimal() != null) ? req.getAnimal().getAnimalId() : "N/A";
                String reqDateStr = (req.getRequestDate() != null) ? req.getRequestDate().toString().substring(0, 10) : "N/A"; // Format date

                model.addRow(new Object[]{
                        req.getRequestId(), animalId, adopterId, adopterName, reqDateStr, req.getStatus()
                });
            }
        } catch (Exception e) { // Catch broader exceptions during loading
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "N/A", "N/A", "Could not load data", "N/A", "N/A"});
        }
    }

    private void sendNotificationInBackground(String userId, String message) {
        if (notificationDAO != null && userId != null && !userId.equals("N/A") && !notificationExecutor.isShutdown()) {
            notificationExecutor.submit(() -> {
                try {
                    // Use the simpler addNotification method
                    notificationDAO.addNotification(userId, message);
                    System.out.println("Notification sent background to user " + userId);
                } catch (SQLException e) {
                    System.err.println("Failed to send notification (background) to user " + userId + ": " + e.getMessage());
                }
            });
        }
    }

    private void processRequest(boolean isApproved) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Please select a request.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }

        // Get data directly from model, ensure correct indices
        String requestId = (String) model.getValueAt(selectedRow, 0);
        String animalId = (String) model.getValueAt(selectedRow, 1);
        String userId = (String) model.getValueAt(selectedRow, 2);
        String currentStatus = (String) model.getValueAt(selectedRow, 5);

        if (adoptionDAO == null || animalDAO == null || notificationDAO == null) { /* Handle DB error */ return; }

        if (!"Pending".equalsIgnoreCase(currentStatus)) { /* Handle already processed */ return; }

        String newStatus = isApproved ? "Approved" : "Rejected";

        try {
            boolean success = adoptionDAO.updateRequestStatus(requestId, newStatus);

            if (success && isApproved && animalId != null && !animalId.equals("N/A")) {
                animalDAO.updateAnimalStatus(animalId, "Adopted");
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "Request successfully " + newStatus + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                String notificationMessage = "Your adoption request for animal ID " + (animalId != null ? animalId : "?") + " has been " + newStatus + ".";
                sendNotificationInBackground(userId, notificationMessage);
                loadAllAdoptionRequests(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update request status.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}