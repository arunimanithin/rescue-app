package com.rescueapp.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a user's request to adopt a specific animal.
 * Keeps minimal logic so it stays a clean POJO for services/DAOs to use.
 */
public class AdoptionRequest {

    // Core identifiers and links
    private String requestId;
    private StrayAnimal animal;   // the animal being requested
    private User adopter;         // the user requesting adoption

    // Timestamps and status
    private Instant requestDate = Instant.now();
    private String status;        // "Pending", "Approved", "Rejected"


    // Constructors
    public AdoptionRequest() {
    }

    public AdoptionRequest(String requestId, StrayAnimal animal, User adopter, Instant requestDate, String status) {
        this.requestId = requestId;
        this.animal = animal;
        this.adopter = adopter;
        if (requestDate != null) this.requestDate = requestDate;
        this.status = status;
    }

    // Convenience factory for common case (auto date, default status Pending)
    public static AdoptionRequest createPending(String requestId, StrayAnimal animal, User adopter) {
        return new AdoptionRequest(requestId, animal, adopter, Instant.now(), "Pending");
    }

    // Domain behavior (minimal workflow helpers)
    public void approve() {
        this.status = "Approved";
    }

    public void reject() {
        this.status = "Rejected";
    }

    public boolean isPending()   { return "Pending".equalsIgnoreCase(status); }
    public boolean isApproved()  { return "Approved".equalsIgnoreCase(status); }
    public boolean isRejected()  { return "Rejected".equalsIgnoreCase(status); }

    // Getters / Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public StrayAnimal getAnimal() { return animal; }
    public void setAnimal(StrayAnimal animal) { this.animal = animal; }

    public User getAdopter() { return adopter; }
    public void setAdopter(User adopter) { this.adopter = adopter; }

    public Instant getRequestDate() { return requestDate; }
    public void setRequestDate(Instant requestDate) { this.requestDate = requestDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Utilities
    @Override
    public String toString() {
        return "\nAdoption Request Details:" +
               "\n---------------------------------" +
               "\nRequest ID     : " + requestId +
               "\nAnimal ID      : " + (animal != null ? animal.getAnimalId() : "N/A") +
               "\nAdopter ID     : " + (adopter != null ? adopter.getUserId() : "N/A") +
               "\nRequest Date   : " + (requestDate != null ? requestDate : "N/A") +
               "\nStatus         : " + (status != null ? status : "N/A") +
               "\n---------------------------------\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdoptionRequest)) return false;
        AdoptionRequest that = (AdoptionRequest) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }
}
