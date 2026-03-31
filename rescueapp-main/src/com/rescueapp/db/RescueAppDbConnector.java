package com.rescueapp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RescueAppDbConnector {

	public RescueAppDbConnector() {
		
	}
	
	public Connection getMySQLConnection() {
		String url = "jdbc:mysql://localhost:3306/strayanimalrescue";
        String user = "root";
        String password = "animals";
        Connection conn = null;
        try {
        	conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to MySQL database!");
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
        return conn;
	}

}
