package com.rescueapp.gui.ngo;

import com.rescueapp.core.Task;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppNotificationDAO;
import com.rescueapp.db.dao.RescueAppTaskDAO;
import com.rescueapp.db.dao.RescueAppUserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class NgoTaskManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppTaskDAO taskDAO;
    private RescueAppUserDAO userDAO;
    private RescueAppNotificationDAO notificationDAO;
    private final ExecutorService notificationExecutor = Executors.newSingleThreadExecutor();

    public NgoTaskManagementPanel() {
        super(new BorderLayout(10, 10));

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            taskDAO = new RescueAppTaskDAO(db);
            userDAO = new RescueAppUserDAO(db);
            notificationDAO = new RescueAppNotificationDAO(db);
        } catch (Exception e) {
             e.printStackTrace();
             
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Manage & Assign Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Task ID", "Description", "Status", "Date", "Assignee Name"};
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
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        add(new JScrollPane(table), BorderLayout.CENTER);

        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh List");
        JLabel assignLabel = new JLabel("Assign Selected To:");
        JComboBox<User> volunteerComboBox = new JComboBox<>();
        JButton assignButton = new JButton("Assign Task");

        populateVolunteerComboBox(volunteerComboBox);

        assignButton.addActionListener(e -> assignSelectedTask(volunteerComboBox));
        refreshButton.addActionListener(e -> loadTasks());

        bottomPanel.add(refreshButton);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(assignLabel);
        bottomPanel.add(volunteerComboBox);
        bottomPanel.add(assignButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTasks();
    }

    private void loadTasks() {
        model.setRowCount(0);
        if (taskDAO == null) return;
        try {
            List<Task> tasks = taskDAO.getAllTasks();
            for (Task task : tasks) {
                model.addRow(new Object[]{
                        task.getTaskId(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getDate(),
                        (task.getAssignee() != null && task.getAssignee().getName() != null)
                            ? task.getAssignee().getName() : "Unassigned"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void populateVolunteerComboBox(JComboBox<User> comboBox) {
        if (userDAO == null) return;
        try {
             List<User> users = userDAO.getAllUsers();
             Vector<User> volunteers = new Vector<>();
             volunteers.add(null); // Option for Unassigned
             for (User u : users) {
                 if (u.getRole().equalsIgnoreCase("Volunteer")) {
                     volunteers.add(u);
                 }
             }
             comboBox.setRenderer(new DefaultListCellRenderer() {
                 @Override
                 public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                     super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     if (value instanceof User) {
                         setText(((User) value).getName());
                     } else {
                         setText("Unassign");
                     }
                     return this;
                 }
             });
             comboBox.setModel(new DefaultComboBoxModel<>(volunteers));
        } catch(SQLException e) { e.printStackTrace(); }
    }

    private void assignSelectedTask(JComboBox<User> volunteerComboBox) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to assign.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String taskId = (String) model.getValueAt(selectedRow, 0);
        String taskDescription = (String) model.getValueAt(selectedRow, 1);
        User selectedVolunteer = (User) volunteerComboBox.getSelectedItem();
        String assigneeId = (selectedVolunteer != null) ? selectedVolunteer.getUserId() : null;

        if (assigneeId == null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to unassign this task?",
                "Confirm Unassignment", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        try {
            boolean success = taskDAO.assignTask(taskId, assigneeId);
            if (success) {
                String successMessage;
                if (selectedVolunteer != null) {
                    String notificationMessage = "You have been assigned a new task: " + taskDescription;
                    sendNotificationToVolunteer(assigneeId, notificationMessage);
                    successMessage = "Task assigned successfully to " + selectedVolunteer.getName() + "!";
                } else {
                     successMessage = "Task successfully unassigned!";
                }
                JOptionPane.showMessageDialog(this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTasks();
            } else {
                 JOptionPane.showMessageDialog(this, "Failed to update assignment in database.", "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "Failed to assign task due to database error.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendNotificationToVolunteer(String userId, String message) {
       if (notificationDAO != null && userId != null && !userId.equals("N/A") && !notificationExecutor.isShutdown()) {
           notificationExecutor.submit(() -> {
               try {
                   System.out.println("Attempting to add notification for user: " + userId + " with message: " + message);
                   boolean added = notificationDAO.addNotification(userId, message);
                   if(added) {
                       System.out.println("Notification successfully added to DB for user: " + userId);
                   } else {
                        System.out.println("!!! Notification failed to add to DB for user: " + userId);
                   }
               } catch (SQLException e) {
                   System.err.println("!!! SQL Error sending notification for user " + userId + ": " + e.getMessage());
                   e.printStackTrace();
               } catch (Exception e) {
                    System.err.println("!!! Unexpected Error sending notification for user " + userId + ": " + e.getMessage());
                    e.printStackTrace();
               }
           });
       } else {
            System.err.println("!!! Could not send notification. DAO:" + (notificationDAO != null) + ", UserID: " + userId + ", ExecutorShutdown:" + (notificationExecutor != null ? notificationExecutor.isShutdown() : "null"));
       }
    }

    public void shutdownExecutor() {
        if (notificationExecutor != null && !notificationExecutor.isShutdown()) {
             System.out.println("NgoTaskManagementPanel: Shutting down notification executor...");
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
}