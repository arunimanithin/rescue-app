package com.rescueapp.db.dao;

import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueAppUserDAO {
    private Connection conn;

    public RescueAppUserDAO(RescueAppDbConnector conn) {
        this.conn =  conn.getMySQLConnection();
    }

    public User addUser(User user) throws SQLException {
    	String sql = "INSERT INTO users (userId, name, email, contact, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
        	ps.setString(1, user.getUserId());
        	ps.setString(2, user.getName());
        	ps.setString(3, user.getEmail());
        	ps.setString(4, user.getContact());
        	ps.setString(5, user.getRole());
            ps.executeUpdate();
        }
        return user;
    }

    public User getUser(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE name=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("contact")
                    );
                }
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getString("userId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("contact")
                ));
            }
        }
        return list;
    }

    
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, contact = ? WHERE userId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());    // 1st '?': name
            ps.setString(2, user.getEmail());   // 2nd '?': email
            ps.setString(3, user.getContact()); // 3rd '?': contact
            ps.setString(4, user.getUserId());  // 4th '?': userId
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM users WHERE userId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        }
    }
}