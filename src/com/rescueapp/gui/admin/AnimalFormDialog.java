package com.rescueapp.gui.admin;

import com.rescueapp.core.StrayAnimal;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

@SuppressWarnings("serial")
public class AnimalFormDialog extends JDialog {

    private JTextField animalIdField;
    private JTextField specificationsField;
    private JTextField photoUrlField;
    private JComboBox<String> statusComboBox;
    private JTextArea medReportArea;

    private StrayAnimal animal; // Null if 'Add', non-null if 'Edit'
    private boolean confirmed = false;

    public AnimalFormDialog(Frame owner, StrayAnimal animalToEdit) {
        super(owner, true); // Modal dialog
        this.animal = animalToEdit;
        
        setTitle(animal == null ? "Add New Animal" : "Edit Animal");
        setSize(500, 600);
        setLocationRelativeTo(owner);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Animal ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createLabel("Animal ID:", labelFont), gbc);
        animalIdField = new JTextField(20);
        animalIdField.setFont(fieldFont);
        if (animal != null) {
            animalIdField.setText(animal.getAnimalId());
            animalIdField.setEditable(false);
        } else {
            animalIdField.setText(UUID.randomUUID().toString());
            animalIdField.setEditable(false);
        }
        gbc.gridx = 1; panel.add(animalIdField, gbc);

        // Specifications
        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("Specifications:", labelFont), gbc);
        specificationsField = new JTextField(20);
        specificationsField.setFont(fieldFont);
        if (animal != null) specificationsField.setText(animal.getSpecifications());
        gbc.gridx = 1; panel.add(specificationsField, gbc);

        // Photo URL
        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("Photo URL:", labelFont), gbc);
        photoUrlField = new JTextField(20);
        photoUrlField.setFont(fieldFont);
        if (animal != null) photoUrlField.setText(animal.getPhotoUrl());
        gbc.gridx = 1; panel.add(photoUrlField, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy++;
        panel.add(createLabel("Status:", labelFont), gbc);
        String[] statuses = {"Available", "Rescued", "Injured", "Adopted"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setFont(fieldFont);
        if (animal != null) statusComboBox.setSelectedItem(animal.getStatus());
        gbc.gridx = 1; panel.add(statusComboBox, gbc);

        // Medical Record
        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(createLabel("Medical Record:", labelFont), gbc);
        medReportArea = new JTextArea(10, 20);
        medReportArea.setFont(fieldFont);
        medReportArea.setLineWrap(true);
        medReportArea.setWrapStyleWord(true);
        if (animal != null) medReportArea.setText(animal.getMedReport());
        JScrollPane scrollPane = new JScrollPane(medReportArea);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        // --- Buttons ---
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> onSave());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancel());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    private void onSave() {
        if (specificationsField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Specifications are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a new animal object or update the existing one
        if (animal == null) {
            animal = new StrayAnimal();
            animal.setAnimalId(animalIdField.getText());
        }
        
        animal.setSpecifications(specificationsField.getText().trim());
        animal.setPhotoUrl(photoUrlField.getText().trim());
        animal.setMedReport(medReportArea.getText().trim());
        animal.setStatus((String) statusComboBox.getSelectedItem());

        confirmed = true;
        dispose();
    }

    private void onCancel() {
        confirmed = false;
        dispose();
    }

    // Public methods to get the results
    public boolean isConfirmed() {
        return confirmed;
    }

    public StrayAnimal getAnimal() {
        return animal;
    }
}