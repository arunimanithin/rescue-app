package com.rescueapp.gui;

import com.rescueapp.core.User;
import com.rescueapp.gui.ngo.NgoAnimalPanel;
import com.rescueapp.gui.ngo.NgoAdoptionRequestPanel;
import com.rescueapp.gui.ngo.NgoReportPanel;
import com.rescueapp.gui.ngo.NgoTaskManagementPanel;
import com.rescueapp.gui.user.MyProfilePanel;
import com.rescueapp.gui.NotificationPanel; // <-- Import NotificationPanel

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class NgoDashboard extends JFrame {

    private User loggedInNgo;
    private NgoAdoptionRequestPanel adoptionPanel;
    private NgoTaskManagementPanel taskPanel;
    // No need for reference to NgoReportPanel if it doesn't have an executor

    public NgoDashboard(User user) {
        this.loggedInNgo = user;

        setTitle("NGO Dashboard - Welcome, " + loggedInNgo.getName());
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Create instances of panels with executors
        adoptionPanel = new NgoAdoptionRequestPanel();
        taskPanel = new NgoTaskManagementPanel();

        // Add tabs
        tabs.add("Animal Management", new NgoAnimalPanel());
        tabs.add("Adoption Requests", adoptionPanel);
        tabs.add("Task Management", taskPanel);
        tabs.add("User Reports", new NgoReportPanel());
        tabs.add("My Profile", new MyProfilePanel(loggedInNgo));
        tabs.add("My Notifications", new NotificationPanel(loggedInNgo)); // <-- Add Notification tab

        add(tabs);

        // Add shutdown hook
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("NgoDashboard closing, shutting down executors...");
                if (adoptionPanel != null) {
                    adoptionPanel.shutdownExecutor();
                }
                if (taskPanel != null) {
                    taskPanel.shutdownExecutor();
                }
                // ReportAnimalPanel executor is shut down by UserDashboard
                super.windowClosing(e);
            }
        });
    }
}