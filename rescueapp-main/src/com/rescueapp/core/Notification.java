package com.rescueapp.core;

import java.time.Instant;
import java.util.Objects;


public class Notification {

    private String notificationId;
    private String message;
    private Notifiable recipient;

    private Instant createdAt = Instant.now();
    private boolean read = false;

    public Notification() {
    }

    public Notification(String notificationId, String message, Notifiable recipient) {
        setNotificationId(notificationId);
        setMessage(message);
        setRecipient(recipient);
    }



    /** Marks the notification as read. */
    public void markAsRead() {
        this.read = true;
    }

    
    public void deliver() {
        if (recipient != null && message != null) {
            recipient.receiveNotification(message);
            markAsRead(); 
        }
    }

   

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
                this.message = message;
    }

    public Notifiable getRecipient() {
        return recipient;
    }

    public void setRecipient(Notifiable recipient) {
        this.recipient = recipient;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

   

    public String toString() {
        return "Notification{" +
                "notificationId='" + notificationId + '\'' +
                ", message='" + message + '\'' +
                ", recipient=" + (recipient == null ? "null" : recipient.getClass().getSimpleName()) +
                ", createdAt=" + createdAt +
                ", read=" + read +
                '}';
    }

   
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(notificationId, that.notificationId);
    }

   
    public int hashCode() {
        return Objects.hash(notificationId);
    }
}
