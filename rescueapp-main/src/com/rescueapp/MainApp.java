package com.rescueapp;

import com.rescueapp.gui.LoginPage;
import javax.swing.SwingUtilities;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
    }
}