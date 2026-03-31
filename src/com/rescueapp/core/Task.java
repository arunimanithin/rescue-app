package com.rescueapp.core;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a work item assigned to a user (typically a Volunteer, sometimes an NGO).
 * Simple POJO with minimal workflow helpers. Database and UI logic belong in services/DAOs.
 */
public class Task {
    private String taskId;
    private String description;
    private String status; // "Open","InProgress","Done"
    private Date date;
    private User assignee; // Volunteer or NGO (both extend User)

    public Task() { }

    public Task(String taskId, String description, String status, Date date, User assignee) {
        this.taskId = taskId;
        this.description = description;
        this.status = status;
        this.date = date;
        this.assignee = assignee;
    }

    // --------- Workflow helpers ---------

    public void open() {
        this.status = "Open";
    }

    public void start() {
        this.status = "InProgress";
    }

    public void complete() {
        this.status = "Done";
    }

    public boolean isOpen()        { return "Open".equalsIgnoreCase(status); }
    public boolean isInProgress()  { return "InProgress".equalsIgnoreCase(status); }
    public boolean isDone()        { return "Done".equalsIgnoreCase(status); }

    // Basic sanity check useful before saving
    public boolean isValid() {
        return taskId != null && !taskId.isBlank()
            && description != null && !description.isBlank()
            && date != null
            && assignee != null;
    }

    // --------- Getters / Setters ---------

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }

    // --------- Utilities ---------

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", date=" + date +
                ", assignee=" + (assignee != null ? assignee.getUserId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}
