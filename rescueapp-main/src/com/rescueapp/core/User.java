package com.rescueapp.core;

import java.util.Objects;

/**
 * Base user in the system. Other roles (Volunteer, NGO, Admin) extend this.
 * Plain POJO: no database or UI logic here.
 */
public class User implements Notifiable {
    private String userId;
    private String name;
    private String email;
    private String role; 
    private String contact;// e.g., "USER", "VOLUNTEER", "NGO", "Admin"

    public User() { }

    public User(String userId, String name, String email, String role, String contact) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.contact = contact;
    }

    // Notifications: simple default behavior
    @Override
    public void receiveNotification(String message) {
        // Minimal default: print to console. Replace with UI hook or NotificationService later.
        System.out.println("[Notification to " + (name != null ? name : userId) + "]: " + message);
    }

    // Getters / Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    // Convenience helpers
    public boolean hasRole(String expected) {
        return expected != null && expected.equalsIgnoreCase(role);
    }

    // Utilities
    @Override
    public String toString() {
        return "\nUser Details:" +
               "\n------------------------" +
               "\nUser ID   : " + userId +
               "\nName      : " + name +
               "\nEmail     : " + email +
               "\nRole      : " + role +
               "\nContact   : " + contact +
               "\n------------------------";
    }


    // Equality by userId (natural identifier)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
