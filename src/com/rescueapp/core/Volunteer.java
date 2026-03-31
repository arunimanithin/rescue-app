package com.rescueapp.core;

import java.util.Objects;

/**
 * Volunteer user with rescue-oriented actions.
 * This class stays framework-free; services/DAOs should handle persistence and notifications.
 */
public class Volunteer extends User {

    private String phone;          // optional contact
    private String availability;   // e.g., "Weekends", "Evenings", "Full-time"

    public Volunteer() {
        super();
        setRole("Volunteer");
    }

    public Volunteer(String id, String name, String email, String contact) {
        super(id, name, email, "Volunteer", contact);
    }

    public Volunteer(String id, String name, String email, String contact, String phone, String availability) {
        super(id, name, email, "Volunteer", contact);
        this.phone = phone;
        this.availability = availability;
    }

    // Role helpers (domain-level intent; services should persist and notify)
    public Report updateReportStatus(Report report, String newStatus) {
        if (report == null) return null;
        report.setStatus(newStatus);
        return report;
    }

    public Task startTask(Task task) {
        if (task != null) task.start();
        return task;
    }

    public Task completeTask(Task task) {
        if (task != null) task.complete();
        return task;
    }

    // Optional: add a simple log line to a report description (demo-friendly)
    public void addLogToReport(Report report, String note) {
        if (report == null || note == null || note.isBlank()) return;
        String desc = report.getDescription();
        String appended = (desc == null || desc.isBlank()) ? ("[Log] " + note)
                : (desc + System.lineSeparator() + "[Log] " + note);
        report.setDescription(appended);
    }

    // Getters / Setters for extra fields
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override
    public String toString() {
        return "\nVolunteer Details:" +
        		"\n--------------------------------\n" +
                "UserId='" + getUserId() + '\n' +
                "Name='" + getName() + '\n' +
                "Email='" + getEmail() + '\n' +
                "Phone='" + phone + '\n' +
                "Availability='" + availability + '\n';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Volunteer)) return false;
        Volunteer that = (Volunteer) o;
        return Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }
}
