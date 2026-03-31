package com.rescueapp.db.dao;

import com.rescueapp.core.AdoptionRequest;
import com.rescueapp.core.StrayAnimal; // Added import
import com.rescueapp.core.User;     // Added import
import com.rescueapp.db.RescueAppDbConnector;

import java.sql.*;
import java.time.Instant; // Added import
import java.util.ArrayList;
import java.util.List;

public class RescueAppAdoptionDAO {
    private Connection conn;

    public RescueAppAdoptionDAO(RescueAppDbConnector conn) {
        this.conn = conn.getMySQLConnection();
    }

    public AdoptionRequest addRequest(AdoptionRequest r) throws SQLException {
        String sql = "INSERT INTO adoption (reqId, reqdate, userId, animalId, name, contact, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getRequestId());
            ps.setString(2, r.getRequestDate().toString());
            ps.setString(3, r.getAdopter().getUserId());
            ps.setString(4, r.getAnimal().getAnimalId());
            ps.setString(5, r.getAdopter().getName());
            ps.setString(6, r.getAdopter().getContact());
            ps.setString(7, r.getStatus()); // Save initial status
            ps.executeUpdate();
        }
        return r;
    }
    
    public boolean updateRequestStatus(String requestId, String status) throws SQLException {
        String sql = "UPDATE adoption SET status = ? WHERE reqId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Corrected method: Fetches all requests with full User and Animal details joined.
     * Uses LEFT JOIN to ensure requests show even if the Animal or User record is missing.
     */
    public List<AdoptionRequest> getAllRequestsWithDetails() throws SQLException {
        List<AdoptionRequest> list = new ArrayList<>();
        
        // Use LEFT JOIN to prevent missing/deleted animals from crashing the query
        String sql = "SELECT a.*, " +
                     "u.userId AS adopterId, u.name AS adopterName, u.email, u.contact, u.role, " +
                     "an.animalId AS animal_id, an.specifications, an.medreport, an.status AS animalStatus " +
                     "FROM adoption a " +
                     "LEFT JOIN users u ON a.userId = u.userId " +
                     "LEFT JOIN animals an ON a.animalId = an.animalId";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AdoptionRequest r = new AdoptionRequest();
                r.setRequestId(rs.getString("reqId"));
                r.setRequestDate(Instant.parse(rs.getString("reqdate")));
                r.setStatus(rs.getString("status"));
                
                
                if (rs.getString("adopterId") != null) {
                    User adopter = new User(
                            rs.getString("adopterId"),
                            rs.getString("adopterName"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("contact")
                    );
                    r.setAdopter(adopter);
                }

               
                if (rs.getString("animal_id") != null) {
                    StrayAnimal animal = new StrayAnimal();
                    animal.setAnimalId(rs.getString("animal_id"));
                    animal.setSpecifications(rs.getString("specifications"));
                    animal.setMedReport(rs.getString("medreport"));
                    animal.setStatus(rs.getString("animalStatus"));
                    r.setAnimal(animal);
                }
                // If animal_id is null, r.getAnimal() remains null, which is handled in the panel.

                list.add(r);
            }
        }
        return list;
    }
    
    // Public method now calls the detailed fetch method
    public List<AdoptionRequest> getAllRequests() throws SQLException {
        return getAllRequestsWithDetails();
    }
}