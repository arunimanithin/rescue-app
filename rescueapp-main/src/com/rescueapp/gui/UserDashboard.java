package com.rescueapp.gui;

import com.rescueapp.core.User;
import com.rescueapp.gui.user.AnimalAdoptionPanel;
import com.rescueapp.gui.user.MyProfilePanel;
import com.rescueapp.gui.NotificationPanel;
import com.rescueapp.gui.user.ReportAnimalPanel; // Make sure this is imported
import java.awt.event.WindowAdapter;             // Import WindowAdapter
import java.awt.event.WindowEvent;              // Import WindowEvent

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class UserDashboard extends JFrame {

    private User loggedInUser;
    private ReportAnimalPanel reportPanel; // <-- Reference to panel with executor

    public UserDashboard(User user) {
        this.loggedInUser = user;

        setTitle("User Dashboard - Welcome, " + loggedInUser.getName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // Good practice

        // Create instances of panels
        reportPanel = new ReportAnimalPanel(loggedInUser); // <-- Create instance

        // Add tabs using instances
        tabs.add("Adopt an Animal", new AnimalAdoptionPanel(loggedInUser));
        tabs.add("Report an Animal", reportPanel); // <-- Add instance
        tabs.add("My Profile", new MyProfilePanel(loggedInUser));
        tabs.add("My Notifications", new NotificationPanel(loggedInUser));

        add(tabs);

        // Add shutdown hook
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("UserDashboard closing, shutting down executor...");
                // --- ADD SHUTDOWN FOR REPORT PANEL ---
                if (reportPanel != null) {
                     reportPanel.shutdownExecutor(); // Call shutdown directly
                }
                super.windowClosing(e);
            }
        });
    }
}