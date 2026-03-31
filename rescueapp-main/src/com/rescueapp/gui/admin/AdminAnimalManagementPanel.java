package com.rescueapp.gui.admin;

import com.rescueapp.core.StrayAnimal;
import com.rescueapp.db.RescueAppDbConnector;
import com.rescueapp.db.dao.RescueAppAnimalDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("serial")
public class AdminAnimalManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private RescueAppAnimalDAO animalDAO;

    
    private JTextField animalIdField = new JTextField(15);
    private JTextArea specificationsArea = new JTextArea(3, 15);
    private JTextField photoUrlField = new JTextField(15);
    private JTextField medReportField = new JTextField(15);
    private JComboBox<String> statusComboBox;
    private JButton addButton = new JButton("Add New Animal");
    private JButton updateButton = new JButton("Update Selected Animal");
    private JButton clearButton = new JButton("Clear Form");

    public AdminAnimalManagementPanel() {
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

        JLabel title = new JLabel("Manage All Stray Animals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columns = {"Animal ID", "Specifications", "Photo URL", "Medical Report", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        addButton.addActionListener(this::handleAddAnimal);
        updateButton.addActionListener(this::handleUpdateAnimal);
        clearButton.addActionListener(e -> clearForm());

        loadAnimals();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Animal Details"));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(createLabel("Animal ID:", labelFont), gbc);
        animalIdField.setFont(fieldFont); animalIdField.setEditable(false);
        gbc.gridx = 1; panel.add(animalIdField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(createLabel("Specifications:", labelFont), gbc);
        specificationsArea.setFont(fieldFont); specificationsArea.setLineWrap(true); specificationsArea.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        panel.add(new JScrollPane(specificationsArea), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy++; panel.add(createLabel("Photo URL:", labelFont), gbc);
        photoUrlField.setFont(fieldFont); gbc.gridx = 1; panel.add(photoUrlField, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(createLabel("Medical Report:", labelFont), gbc);
        medReportField.setFont(fieldFont); gbc.gridx = 1; panel.add(medReportField, gbc);

        gbc.gridx = 0; gbc.gridy++; panel.add(createLabel("Status:", labelFont), gbc);
        String[] statuses = {"Available", "Rescued", "Injured", "Adopted", "In Treatment"};
        statusComboBox = new JComboBox<>(statuses); statusComboBox.setFont(fieldFont);
        gbc.gridx = 1; panel.add(statusComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); buttonPanel.setBackground(Color.WHITE);
        addButton.setFont(fieldFont); updateButton.setFont(fieldFont); clearButton.setFont(fieldFont);
        buttonPanel.add(addButton); buttonPanel.add(updateButton); buttonPanel.add(clearButton);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        clearForm();
        return panel;
    }

    private void loadAnimals() {
        model.setRowCount(0);
        if (animalDAO == null) return;
        try {
            List<StrayAnimal> animals = animalDAO.getAllAnimals();
            for (StrayAnimal a : animals) {
                model.addRow(new Object[]{ a.getAnimalId(), a.getSpecifications(), a.getPhotoUrl(), a.getMedReport(), a.getStatus() });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading animals.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        animalIdField.setText((String) model.getValueAt(row, 0));
        specificationsArea.setText((String) model.getValueAt(row, 1));
        photoUrlField.setText((String) model.getValueAt(row, 2));
        medReportField.setText((String) model.getValueAt(row, 3));
        statusComboBox.setSelectedItem(model.getValueAt(row, 4));
        addButton.setEnabled(false); updateButton.setEnabled(true); animalIdField.setEditable(false);
    }

    private void clearForm() {
        animalIdField.setText("[Auto-Generated]"); specificationsArea.setText(""); photoUrlField.setText("");
        medReportField.setText(""); statusComboBox.setSelectedIndex(0); table.clearSelection();
        addButton.setEnabled(true); updateButton.setEnabled(false); animalIdField.setEditable(false);
    }

    private void handleAddAnimal(ActionEvent e) {
        if (specificationsArea.getText().trim().isEmpty() || medReportField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Specifications and Medical Report required.", "Input Error", JOptionPane.WARNING_MESSAGE); return;
        }
        StrayAnimal animal = new StrayAnimal();
        animal.setAnimalId(UUID.randomUUID().toString());
        animal.setSpecifications(specificationsArea.getText().trim()); animal.setPhotoUrl(photoUrlField.getText().trim());
        animal.setMedReport(medReportField.getText().trim()); animal.setStatus((String) statusComboBox.getSelectedItem());
        try {
            animalDAO.addAnimal(animal);
            JOptionPane.showMessageDialog(this, "Animal added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAnimals(); clearForm();
        } catch (SQLException ex) {
            ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Failed to add animal.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateAnimal(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Please select an animal to update.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        if (specificationsArea.getText().trim().isEmpty() || medReportField.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Specifications and Medical Report required.", "Input Error", JOptionPane.WARNING_MESSAGE); return; }
        String animalId = animalIdField.getText(); String newStatus = (String) statusComboBox.getSelectedItem();
        // Add logic here to update other fields if an updateAnimal(StrayAnimal) method exists in DAO
        try {
            boolean success = animalDAO.updateAnimalStatus(animalId, newStatus);
            if (success) {
                 JOptionPane.showMessageDialog(this, "Animal status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                 loadAnimals(); clearForm();
            } else { JOptionPane.showMessageDialog(this, "Failed to update animal status.", "Update Failed", JOptionPane.ERROR_MESSAGE); }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Database error during update.", "Database Error", JOptionPane.ERROR_MESSAGE); }
    }

    private JLabel createLabel(String text, Font font) { JLabel label = new JLabel(text); label.setFont(font); return label;
    
    }
}