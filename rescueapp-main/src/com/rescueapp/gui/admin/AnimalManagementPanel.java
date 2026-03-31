package com.rescueapp.gui.admin;

import com.rescueapp.core.StrayAnimal;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppAnimalDAO;
import com.rescueapp.gui.util.ImageRenderer; // <-- Import Image Renderer

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class AnimalManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppAnimalDAO animalDAO;

    public AnimalManagementPanel() {
        super(new BorderLayout(10, 10));
        
        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            animalDAO = new RescueAppAnimalDAO(db);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to Animal DB.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Manage All Animals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // --- COLUMNS UPDATED ---
        String[] columns = {"Animal ID", "Photo", "Specifications", "Status", "Medical Record"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(100); // Set row height for images
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- HIDE THE 'Animal ID' COLUMN ---
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // --- APPLY IMAGE RENDERER AND SET COLUMN SIZES ---
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Photo
        table.getColumnModel().getColumn(2).setPreferredWidth(450); // Specifications (Increased)
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(150); // Medical Record (Reduced)

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons Panel (Unchanged)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Animal");
        addButton.addActionListener(e -> addAnimal());
        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editAnimal());
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteAnimal());
        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadAnimals();
    }

    // --- (loadAnimals, addAnimal, editAnimal, deleteAnimal methods are unchanged from previous corrected version) ---
    private void loadAnimals() {
        model.setRowCount(0);
        if (animalDAO == null) return;
        try {
            List<StrayAnimal> animals = animalDAO.getAllAnimals();
            for (StrayAnimal animal : animals) {
                model.addRow(new Object[]{
                        animal.getAnimalId(), animal.getPhotoUrl(),
                        animal.getSpecifications(), animal.getStatus(), animal.getMedReport()
                });
            }
        } catch (SQLException e) { /* ... error handling ... */ }
    }
    private void addAnimal() { /* ... unchanged ... */ }
    private void editAnimal() { /* ... unchanged ... */ }
    private void deleteAnimal() { /* ... unchanged ... */ }
}