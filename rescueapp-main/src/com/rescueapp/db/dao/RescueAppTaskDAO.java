package com.rescueapp.db.dao;

import com.rescueapp.core.Task;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueAppTaskDAO {
    private Connection conn;

    public RescueAppTaskDAO(RescueAppDbConnector conn) {
        this.conn = conn.getMySQLConnection();
    }

   
    public List<Task> getTasksForVolunteer(String volunteerId) throws SQLException {
        List<Task> taskList = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE userId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, volunteerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setTaskId(rs.getString("taskId"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setDate(rs.getDate("date"));

                    User assignee = new User();
                    assignee.setUserId(rs.getString("userId"));
                    task.setAssignee(assignee);

                    taskList.add(task);
                }
            }
        }
        return taskList;
    }
    
    public boolean assignTask(String taskId, String assigneeId) throws SQLException {
        // Your table uses 'userId' for assignee
        String sql = "UPDATE tasks SET userId = ? WHERE taskId = ?"; 
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assigneeId);
            ps.setString(2, taskId);
            return ps.executeUpdate() > 0;
        }
    }
    
    // ... (rest of the DAO class)
    public boolean updateTaskStatus(String taskId, String newStatus) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE taskId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, taskId);
            return ps.executeUpdate() > 0;
        }
    }

    
    public Task addTask(Task task) throws SQLException {
        // Ensure the 'tasks' table has columns: taskId, description, userId, date, status
        String sql = "INSERT INTO tasks (taskId, description, userId, date, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTaskId());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getAssignee() != null ? task.getAssignee().getUserId() : null); // Handle unassigned
            ps.setDate(4, task.getDate() != null ? new java.sql.Date(task.getDate().getTime()) : null);
            ps.setString(5, task.getStatus());
            ps.executeUpdate();
        }
        return task;
    }

    
    public List<Task> getAllTasks() throws SQLException {
        List<Task> taskList = new ArrayList<>();
        String sql = "SELECT t.*, u.name as assigneeName " +
                     "FROM tasks t LEFT JOIN users u ON t.userId = u.userId " +
                     "ORDER BY t.date DESC, t.taskId DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = new Task();
                task.setTaskId(rs.getString("taskId"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setDate(rs.getDate("date"));

                User assignee = null;
                String assigneeId = rs.getString("userId");
                if (assigneeId != null) {
                    assignee = new User();
                    assignee.setUserId(assigneeId);
                    assignee.setName(rs.getString("assigneeName"));
                }
                task.setAssignee(assignee);

                taskList.add(task);
            }
        }
        return taskList;
    }
}