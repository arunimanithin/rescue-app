package com.rescueapp.gui.user;

import com.rescueapp.core.AdoptionRequest;
import com.rescueapp.core.StrayAnimal;
import com.rescueapp.core.User;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppAdoptionDAO;
import com.rescueapp.db.dao.RescueAppAnimalDAO;
import com.rescueapp.gui.util.ImageRenderer; // <-- Import the new renderer

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("serial")
public class AnimalAdoptionPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppAnimalDAO animalDAO;
    private RescueAppAdoptionDAO adoptionDAO;
    
    private User loggedInUser;

    public AnimalAdoptionPanel(User user) {
        super(new BorderLayout(10, 10));
        this.loggedInUser = user; 

        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            animalDAO = new RescueAppAnimalDAO(db);
            adoptionDAO = new RescueAppAdoptionDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Animals Available for Adoption");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // --- 1. TABLE MODEL UPDATED ---
        // We include Animal ID for logic, but hide it from the user.
        String[] columns = {"Animal ID", "Photo", "Specifications", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        
        // --- 2. UI & APPEARANCE CHANGES ---
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(100); // <-- Set fixed row height for images
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- 3. HIDE THE 'Animal ID' COLUMN ---
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // --- 4. APPLY IMAGE RENDERER AND SET COLUMN SIZES ---
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Photo column
        table.getColumnModel().getColumn(2).setPreferredWidth(450); // Specifications column (Bigger)
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Status column

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Adoption Button Panel (No changes) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton adoptButton = new JButton("Request to Adopt Selected Animal");
        adoptButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adoptButton.setBackground(new Color(40, 180, 90));
        adoptButton.setForeground(Color.WHITE);
        
        bottomPanel.add(adoptButton);
        add(bottomPanel, BorderLayout.SOUTH);

        adoptButton.addActionListener(e -> handleAdoptionRequest());
        loadAvailableAnimals();
    }

    private void loadAvailableAnimals() {
        model.setRowCount(0); // Clear table
        if (animalDAO == null) return;

        try {
            List<StrayAnimal> allAnimals = animalDAO.getAllAnimals();
            
            for (StrayAnimal animal : allAnimals) {
                if (animal.getStatus() != null && animal.getStatus().equalsIgnoreCase("available")) {
                    // --- 5. UPDATED ROW DATA (matches new columns) ---
                    // Add photourl to the "Photo" column
                    model.addRow(new Object[]{
                            animal.getAnimalId(),    // Column 0 (Hidden)
                            animal.getPhotoUrl(),    // Column 1 (Photo)
                            animal.getSpecifications(),// Column 2 (Specifications)
                            animal.getStatus()       // Column 3 (Status)
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            model.addRow(new Object[]{"Error", null, "Could not load animals", ""});
        }
    }

    private void handleAdoptionRequest() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an animal from the table first.", "No Animal Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (adoptionDAO == null) {
             JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- 6. GET ANIMAL ID FROM HIDDEN COLUMN ---
        // This still works because column 0 is the Animal ID
        String animalId = (String) model.getValueAt(selectedRow, 0); 

        // Create a new StrayAnimal object just with the ID
        StrayAnimal selectedAnimal = new StrayAnimal();
        selectedAnimal.setAnimalId(animalId);
        
        // --- (Rest of the method is unchanged) ---
        AdoptionRequest request = new AdoptionRequest();
        request.setRequestId(UUID.randomUUID().toString()); 
        request.setAnimal(selectedAnimal);
        request.setAdopter(loggedInUser); 
        request.setRequestDate(Instant.now());
        request.setStatus("Pending"); 

        try {
            adoptionDAO.addRequest(request);
            
            JOptionPane.showMessageDialog(this, 
                "Adoption Request Submitted!\nYour request for animal " + animalId + " is now pending review.", 
                "Request Submitted", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to submit adoption request. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}