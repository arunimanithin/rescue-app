package com.rescueapp.db.dao;

import com.rescueapp.core.NGO;
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RescueAppNGODAO {
    private Connection conn;

    public RescueAppNGODAO(RescueAppDbConnector conn) {
        this.conn =  conn.getMySQLConnection();
    }

    public NGO addNGO(NGO ngo) throws SQLException {
        String sql = "INSERT INTO ngos (userId, name, email, contact) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ngo.getUserId());
            ps.setString(2, ngo.getName());
            ps.setString(3, ngo.getEmail());
            ps.setString(4, ngo.getContact());
            ps.executeUpdate();
        }
        return ngo;
    }

    public NGO getNgo(String userId) throws SQLException {
        String sql = "SELECT * FROM ngos WHERE userId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new NGO(
                        rs.getString("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("contact")
                    );
                }
            }
        }
        return null;
    }

    public List<NGO> getAllNgos() throws SQLException {
        List<NGO> list = new ArrayList<>();
        String sql = "SELECT * FROM ngos";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new NGO(
                    rs.getString("userId"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("contact")
                ));
            }
        }
        return list;
    }

    public boolean updateNGO(NGO user) throws SQLException {
        String sql = "UPDATE ngos SET name=?, email=?, role=? WHERE userId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM ngos WHERE userId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            return ps.executeUpdate() > 0;
        }
    }
}