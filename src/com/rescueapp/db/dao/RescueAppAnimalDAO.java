package com.rescueapp.db.dao;

import com.rescueapp.core.StrayAnimal;
import com.rescueapp.db.RescueAppDbConnector;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class RescueAppAnimalDAO {
	private Connection conn; 

	public RescueAppAnimalDAO(RescueAppDbConnector conn) {
		this.conn =  conn.getMySQLConnection();
	}

    public StrayAnimal addAnimal(StrayAnimal a) throws SQLException {
        String sql = "INSERT INTO animals (animalId, specifications, photourl, medreport, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAnimalId());
            ps.setString(2, a.getSpecifications());
            ps.setString(3, a.getPhotoUrl());
            ps.setString(4, a.getMedReport());
            ps.setString(5, a.getStatus());
            ps.executeUpdate();
        }
        return a;
    }
   
    public boolean updateAnimalStatus(String animalId, String status) throws SQLException {
        String sql = "UPDATE animals SET status = ? WHERE animalId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, animalId);
            return ps.executeUpdate() > 0; // Returns true if a row was updated
        }
    }
    public boolean updateAnimal(StrayAnimal a) throws SQLException {
        String sql = "UPDATE animals SET specifications = ?, photourl = ?, medreport = ?, status = ? WHERE animalId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getSpecifications());
            ps.setString(2, a.getPhotoUrl());
            ps.setString(3, a.getMedReport());
            ps.setString(4, a.getStatus());
            ps.setString(5, a.getAnimalId());
            return ps.executeUpdate() > 0;
        }
    }
    
    public boolean deleteAnimal(String animalId) throws SQLException {
        String sql = "DELETE FROM animals WHERE animalId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, animalId);
            return ps.executeUpdate() > 0;
        }
    }
    
    public List<StrayAnimal> getAllAnimals() throws SQLException {
        List<StrayAnimal> list = new ArrayList<>();
        String sql = "SELECT * FROM animals";
        try (
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                StrayAnimal a = new StrayAnimal();
                a.setAnimalId(rs.getString("animalId"));
                a.setSpecifications(rs.getString("specifications"));
                a.setPhotoUrl(rs.getString("photourl"));
                a.setMedReport(rs.getString("medreport"));
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
        }
        return list;
    }
}



