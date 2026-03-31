package com.rescueapp.core;

import java.util.Date;
import java.util.Objects;

/**
 * A report filed about a specific stray animal.
 * POJO with minimal behavior; persistence and complex workflows belong in services/DAOs.
 */
public class Report {
    private String reportId;
    private StrayAnimal animal;   // the animal this report is about
    private User reporter;        // who reported it
    private Date date;            // when the report was filed
    private String description;   // free-text details
    private String status = "Open"; // "Open", "In-Progress", "Resolved", etc.
    private String photoUrl;
    private String location;
    private String urgency;

    public Report() { }

    public Report(String reportId, StrayAnimal animal, User reporter, Date date, String description, String photoUrl, String location, String urgency) {
        this.reportId = reportId;
        this.animal = animal;
        this.reporter = reporter;
        this.date = date;
        this.description = description;
        this.photoUrl = photoUrl;
        this.location = location;
        this.urgency = urgency;
    }

    // Minimal workflow helpers
    public void markInProgress() {
        this.status = "In-Progress";
    }

    public void resolve() {
        this.status = "Resolved";
    }

    public boolean isOpen()        { return "Open".equalsIgnoreCase(status); }
    public boolean isInProgress()  { return "In-Progress".equalsIgnoreCase(status); }
    public boolean isResolved()    { return "Resolved".equalsIgnoreCase(status); }

    // Basic sanity check (optional)
    public boolean isValid() {
        return reportId != null && !reportId.isBlank()
            && animal != null
            && reporter != null
            && date != null
            && description != null && !description.isBlank()
            && photoUrl != null && !photoUrl.isBlank()
            && location != null && !location.isBlank()
            && urgency != null && !urgency.isBlank();
    }

    // Getters / Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public StrayAnimal getAnimal() { return animal; }
    public void setAnimal(StrayAnimal animal) { this.animal = animal; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    

    // Utilities
    @Override
    public String toString() {
        return "🐾 Report Details 🐾\n" +
               "----------------------\n" +
               "Report ID     : " + reportId + "\n" +
               "Animal ID     : " + (animal != null ? animal.getAnimalId() : "N/A") + "\n" +
               "Reporter ID   : " + (reporter != null ? reporter.getUserId() : "N/A") + "\n" +
               "Date          : " + date + "\n" +
               "Status        : " + status + "\n" +
               "Description   : " + description + "\n" +
               "Photo URL     : " + photoUrl + "\n" +
               "Location      : " + location + "\n" +
               "Urgency       : " + urgency + "\n";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Report)) return false;
        Report report = (Report) o;
        return Objects.equals(reportId, report.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }
}
