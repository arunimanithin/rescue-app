package com.rescueapp.core;

import java.util.Objects;

/**
 * Represents an NGO account with abilities commonly needed in this app,
 * such as assigning tasks and processing adoption requests.
 * This class stays a simple POJO; actual persistence and complex workflows
 * belong in service/DAO layers.
 */
public class NGO extends User {

    // Optional NGO-specific fields
    private String organizationName;
    private String contactNumber;

    public NGO() {
        super();
        setRole("NGO");
    }

    public NGO(String id, String name, String email, String contact) {
        super(id, name, email, "NGO", contact);
        this.organizationName = name; // default org name to account name
    }

    public NGO(String id, String name, String email, String organizationName, String contactNumber) {
        super(id, name, email, "NGO", contactNumber);
        this.organizationName = organizationName;
        this.contactNumber = contactNumber;
    }

    // Minimal domain actions (delegate heavy logic to services)
    /**
     * Prepare a Task to be assigned to a volunteer.
     * Actual persistence/dispatching should be handled by a service.
     */
    public Task createTask(String taskId, String description, Volunteer assignee) {
        Task t = new Task();
        t.setTaskId(taskId);
        t.setDescription(description);
        t.setAssignee(assignee);
        t.setStatus("Open");
        return t;
    }

    /**
     * Process an adoption request decision at the domain level.
     * Services should call this, then persist changes and send notifications.
     */
    public void processAdoptionRequest(AdoptionRequest request, boolean approve) {
        if (request == null) return;
        if (approve) {
            request.approve();
        } else {
            request.reject();
        }
    }

    // Getters / Setters for NGO-specific fields
    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Utilities
    @Override
    public String toString() {
        return "\nNGO Details:"
        		+ "\n--------------------------------\n"+
                "UserId='" + getUserId() + '\n' +
                "Name='" + getName() + '\n' +
                "Email='" + getEmail() + '\n' +
                "OrganizationName='" + organizationName + '\n' +
                "ContactNumber='" + contactNumber + '\n';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NGO)) return false;
        NGO ngo = (NGO) o;
        return Objects.equals(getUserId(), ngo.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }
}
