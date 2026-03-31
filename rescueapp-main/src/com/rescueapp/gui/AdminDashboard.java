package com.rescueapp.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// --- Imports updated ---
import com.rescueapp.gui.admin.UserManagementPanel;
import com.rescueapp.gui.admin.AdoptionRequestPanel; // Panel can be view-only now
import com.rescueapp.gui.admin.AnimalManagementPanel; 
import com.rescueapp.gui.admin.ReportViewPanel; // <-- New view-only report panel

@SuppressWarnings("serial")
public class AdminDashboard extends JFrame {
    
    // Keep reference only if needed for shutdown (Adoption panel uses executor)
    private AdoptionRequestPanel adoptionPanel; 

    public AdminDashboard() {
        setTitle("Admin Dashboard - RescueApp");
        setSize(1200, 750); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // --- Create panels (Adoption panel is now view-only by default) ---
        adoptionPanel = new AdoptionRequestPanel(); // <-- Uses new default constructor (no buttons)
        
        // --- ADD REQUIRED TABS ONLY ---
        tabs.add("User Management", new UserManagementPanel());
        tabs.add("Animal Management", new AnimalManagementPanel()); 
        tabs.add("View Adoption Requests", adoptionPanel); // <-- View-only
        tabs.add("View User Reports", new ReportViewPanel()); // <-- View-only

        add(tabs);
        
        // Add shutdown hook for the adoption panel's executor
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Admin Dashboard closing, shutting down executor...");
                if (adoptionPanel != null) {
                    adoptionPanel.shutdownExecutor();
                }
                super.windowClosing(e);
            }
        });
    }

    // No longer need createStubPanel
}