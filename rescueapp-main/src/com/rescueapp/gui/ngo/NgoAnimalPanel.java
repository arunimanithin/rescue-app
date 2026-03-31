package com.rescueapp.gui.ngo;

import com.rescueapp.gui.admin.AnimalFormDialog; 
import com.rescueapp.gui.util.ImageRenderer;
import com.rescueapp.core.StrayAnimal;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppAnimalDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class NgoAnimalPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private RescueAppAnimalDAO animalDAO;

    public NgoAnimalPanel() {
        super(new BorderLayout(10, 10));
        try {
            RescueAppDbConnector db = new RescueAppDbConnector();
            animalDAO = new RescueAppAnimalDAO(db);
        } catch (Exception e) { e.printStackTrace(); }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel title = new JLabel("Manage Animals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        String[] columns = {"Animal ID", "Photo", "Specifications", "Status", "Medical Record"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(100);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // --- COLUMN ADJUSTMENTS ---

        // Hide Animal ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        
        // Set Photo Renderer
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        
        // --- THESE ARE THE CHANGED WIDTHS ---
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Photo
        table.getColumnModel().getColumn(2).setPreferredWidth(450); // Specifications (Increased)
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(4).setPreferredWidth(150); // Medical Record (Reduced)
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- (Rest of the file is unchanged) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Animal");
        addButton.addActionListener(e -> addAnimal());
        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editAnimal());
        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        add(bottomPanel, BorderLayout.SOUTH);
        loadAnimals();
    }
    
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
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    private void addAnimal() {
        AnimalFormDialog dialog = new AnimalFormDialog(null, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                animalDAO.addAnimal(dialog.getAnimal());
                loadAnimals();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    private void editAnimal() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;
        String animalId = (String) model.getValueAt(selectedRow, 0);
        try {
            StrayAnimal animalToEdit = animalDAO.getAllAnimals().stream()
                .filter(a -> a.getAnimalId().equals(animalId))
                .findFirst().orElse(null);
            if (animalToEdit == null) return;
            AnimalFormDialog dialog = new AnimalFormDialog(null, animalToEdit);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                animalDAO.updateAnimal(dialog.getAnimal());
                loadAnimals();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}