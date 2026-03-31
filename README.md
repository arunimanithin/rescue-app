# Stray Animal Rescue and Protection App

## Overview
This is a Java-based application developed using Java Swing for the GUI, MySQL for the database, and JDBC for database connectivity. The application aims to support the rescue, adoption, and protection of stray animals by enabling users such as volunteers, NGOs, and administrators to manage animal information, adoption requests, and tasks related to animal welfare.

## Features
- Manage stray animal profiles with details like species, health status, and location.
- Submit and track adoption requests for rescued animals.
- Volunteer and NGO management modules.
- Notifications and reporting system for rescue operations and animal welfare.
- Administrative controls for overseeing system users and animal records.

## Technologies Used
- Java Swing for the user interface.
- MySQL for data persistence.
- JDBC for database connectivity.
- Structured packages following Java best practices:
  - `com.rescueapp.core` for core model classes.
  - Additional packages for GUI and utility classes (to be implemented).

## Project Structure
- `AdoptionRequest.java` - Handles adoption request data.
- `StrayAnimal.java` - Represents the stray animal entity.
- `User.java`, `Volunteer.java`, `SystemAdministrator.java` - User roles and permissions.
- `Notification.java`, `Report.java`, `Task.java` - Support classes for communication and task management.
- `NGO.java` - Represents non-governmental organizations involved.

## Setup and Usage
1. Install JDK 11 or higher.
2. Set up a MySQL database and run the provided SQL scripts (to be added).
3. Configure JDBC connection details in the project.
4. Import the project into an IDE like Eclipse or IntelliJ.
5. Build and run the application.

## Future Work
- Add GUI implementation with Swing.
- Complete database schema and integration.
- Implement authentication and authorization modules.
- Enhance UX with better forms and reporting features.

## Contribution
Feel free to fork the repository and submit pull requests. For any issues or feature requests, please open an issue.


This project serves as an academic and practical exercise in software development, focusing on Java desktop applications integrated with databases to address social issues.

