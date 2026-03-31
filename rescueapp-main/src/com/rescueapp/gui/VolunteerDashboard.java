package com.rescueapp.gui;

import com.rescueapp.core.User;
import com.rescueapp.gui.volunteer.VolunteerTaskPanel;
import com.rescueapp.gui.volunteer.VolunteerReportPanel;
import com.rescueapp.gui.user.MyProfilePanel;
import com.rescueapp.gui.NotificationPanel; // <-- Import NotificationPanel

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class VolunteerDashboard extends JFrame {

    private User loggedInVolunteer;

    public VolunteerDashboard(User user) {
        this.loggedInVolunteer = user;

        setTitle("Volunteer Dashboard - Welcome, " + loggedInVolunteer.getName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // Good practice

        tabs.add("My Tasks", new VolunteerTaskPanel(loggedInVolunteer));
        tabs.add("Manage Reports", new VolunteerReportPanel(loggedInVolunteer));
        tabs.add("My Profile", new MyProfilePanel(loggedInVolunteer));
        tabs.add("My Notifications", new NotificationPanel(loggedInVolunteer)); // <-- Add Notification tab

        add(tabs);
        // No executors to shut down in this dashboard yet
    }
}