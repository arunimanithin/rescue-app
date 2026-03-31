package com.rescueapp.db.dao;

import com.rescueapp.core.User;
import com.rescueapp.core.Volunteer;
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueAppVolunteerDAO {
    private Connection conn;

        public RescueAppVolunteerDAO(RescueAppDbConnector conn) {
            this.conn =  conn.getMySQLConnection();
        }

        public Volunteer addVolunteer(Volunteer volunteer) throws SQLException {
            
            try (PreparedStatement psUser = conn.prepareStatement(
                    "INSERT INTO users (userId, name, email, contact, role) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), contact=VALUES(contact)")) {
                psUser.setString(1, volunteer.getUserId());
                psUser.setString(2, volunteer.getName());
                psUser.setString(3, volunteer.getEmail());
                psUser.setString(4, volunteer.getContact());
                psUser.setString(5, "Volunteer"); // Set role explicitly
                psUser.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Warning: Could not insert/update user record for volunteer: " + e.getMessage());
                
            }

            
            String sql = "INSERT INTO volunteers (userId, name, email, contact, availability) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, volunteer.getUserId());
                ps.setString(2, volunteer.getName());
                ps.setString(3, volunteer.getEmail());
                ps.setString(4, volunteer.getContact());
                ps.setString(5, volunteer.getAvailability());
                ps.executeUpdate();
            }
            return volunteer;
        }

       
        public Volunteer getVolunteer(String userId) throws SQLException {
            String sql = "SELECT v.userId, v.name, v.email, v.contact, v.availability, u.role " +
                         "FROM volunteers v LEFT JOIN users u ON v.userId = u.userId WHERE v.userId=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Create Volunteer object using specific constructor if available
                        Volunteer vol = new Volunteer(
                            rs.getString("userId"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("contact")
                            );
                        vol.setAvailability(rs.getString("availability"));
                        // vol.setRole(rs.getString("role")); // Role comes from User table potentially
                        return vol;
                    }
                }
            }
            return null;
        }

        
        public List<Volunteer> getAllVolunteers() throws SQLException {
            List<Volunteer> list = new ArrayList<>();
            String sql = "SELECT v.userId, v.name, v.email, v.contact, v.availability, u.role " +
                         "FROM volunteers v LEFT JOIN users u ON v.userId = u.userId";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                     Volunteer vol = new Volunteer(
                        rs.getString("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("contact")
                     );
                     vol.setAvailability(rs.getString("availability"));
                     // vol.setRole(rs.getString("role"));
                     list.add(vol);
                }
            }
            return list;
        }

        
        public boolean updateVolunteer(Volunteer volunteer) throws SQLException {
            // Update users table
             try (PreparedStatement psUser = conn.prepareStatement(
                    "UPDATE users SET name = ?, email = ?, contact = ? WHERE userId = ?")) {
                psUser.setString(1, volunteer.getName());
                psUser.setString(2, volunteer.getEmail());
                psUser.setString(3, volunteer.getContact());
                psUser.setString(4, volunteer.getUserId());
                psUser.executeUpdate(); // Don't check return value here, main update is below
            } catch (SQLException e) {
                 System.err.println("Warning: Could not update user record for volunteer: " + e.getMessage());
                 // Decide if this should be fatal
            }

            // Update volunteers table
            String sql = "UPDATE volunteers SET name=?, email=?, contact=?, availability=? WHERE userId=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, volunteer.getName());
                ps.setString(2, volunteer.getEmail());
                ps.setString(3, volunteer.getContact());
                ps.setString(4, volunteer.getAvailability());
                ps.setString(5, volunteer.getUserId());
                return ps.executeUpdate() > 0; // Return true if volunteer table was updated
            }
        }

        
        public boolean deleteVolunteer(String userId) throws SQLException {
            boolean deletedFromVolunteers = false;
            // Delete from volunteers table first
            String sqlVol = "DELETE FROM volunteers WHERE userId=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVol)) {
                ps.setString(1, userId);
                deletedFromVolunteers = ps.executeUpdate() > 0;
            }

            
            if (deletedFromVolunteers) {
                String sqlUser = "DELETE FROM users WHERE userId=?";
                 try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                    ps.setString(1, userId);
                    ps.executeUpdate(); // We assume this works if the volunteer existed
                 } catch (SQLException e) {
                     System.err.println("Warning: Could not delete user record for volunteer: " + e.getMessage());
                     }
            }
            return deletedFromVolunteers;
        }
    }