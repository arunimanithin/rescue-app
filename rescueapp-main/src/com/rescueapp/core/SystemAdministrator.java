package com.rescueapp.core;

import java.util.List;
import java.util.Objects;

/**
 * System administrator with elevated privileges.
 * Domain class exposes admin-oriented operations.
 * Heavy lifting (DB, validation, notifications) should be done in services/DAOs.
 */
public class SystemAdministrator extends User {

    public SystemAdministrator() {
        super();
        setRole("Admin");
    }

    public SystemAdministrator(String id, String name, String email, String contact) {
        super(id, name, email, "Admin", contact);
    }

    // ----------------- Delegation-friendly admin operations -----------------
    // These overloads accept service interfaces so the entity remains a POJO.

    public User createUser(UserService svc, User u) {
        return svc.create(u);
    }

    public User updateUser(UserService svc, User u) {
        return svc.update(u);
    }

    public void deleteUser(UserService svc, String userId) {
        svc.delete(userId);
    }

    public List<User> listUsers(UserService svc) {
        return svc.findAll();
    }

    public Volunteer createVolunteer(UserService svc, Volunteer v) {
        return (Volunteer) svc.create(v);
    }

    public NGO createNGO(UserService svc, NGO n) {
        return (NGO) svc.create(n);
    }

    public StrayAnimal registerAnimal(AnimalService svc, StrayAnimal a) {
        return svc.registerAnimal(a);
    }

    public StrayAnimal updateAnimalStatus(AnimalService svc, String animalId, String status) {
        return svc.updateStatus(animalId, status);
    }

    public Report openReport(ReportService svc, Report r) {
        return svc.createReport(r);
    }

    public Report setReportStatus(ReportService svc, String reportId, String status) {
        return svc.updateStatus(reportId, status);
    }

    public AdoptionRequest decideAdoption(AdoptionService svc, String requestId, String decisionNote, boolean approve) {
        return svc.processDecision(requestId, approve, decisionNote);
    }

    public Task assignTask(TaskService svc, Task task) {
        return svc.assign(task);
    }

    public void closeTask(TaskService svc, String taskId) {
        svc.close(taskId);
    }

    // ----------------- Backward-compatible stubs -----------------
    // If UI code already calls these parameterless methods, keep them as safe no-ops
    // or throw UnsupportedOperationException to indicate they must be wired to services.

    public void manageUser() {
        // Intentionally left blank: use createUser/updateUser/deleteUser/listUsers with a UserService.
        // throw new UnsupportedOperationException("Use UserService overloads for admin user management.");
    }

    public void manageVolunteer() {
        // Intentionally blank; prefer service-based overloads.
    }

    public void manageNGO() {
        // Intentionally blank; prefer service-based overloads.
    }

    public void manageStrayAnimal() {
        // Intentionally blank; prefer registerAnimal/updateAnimalStatus with AnimalService.
    }

    public void manageReport() {
        // Intentionally blank; prefer openReport/setReportStatus with ReportService.
    }

    public void manageAdoptionRequest() {
        // Intentionally blank; prefer decideAdoption with AdoptionService.
    }

    public void manageTask() {
        // Intentionally blank; prefer assignTask/closeTask with TaskService.
    }

    // ----------------- Utilities -----------------

    @Override
    public String toString() {
        return "SystemAdministrator{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemAdministrator)) return false;
        SystemAdministrator that = (SystemAdministrator) o;
        return Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }

    // ----------------- Minimal service interfaces -----------------
    // Define slim interfaces the UI/integration layer can implement or mock.
    // Place these in separate files if preferred.

    public interface UserService {
        User create(User u);
        User update(User u);
        void delete(String userId);
        List<User> findAll();
    }

    public interface AnimalService {
        StrayAnimal registerAnimal(StrayAnimal a);
        StrayAnimal updateStatus(String animalId, String status);
    }

    public interface ReportService {
        Report createReport(Report r);
        Report updateStatus(String reportId, String status);
    }

    public interface AdoptionService {
        AdoptionRequest processDecision(String requestId, boolean approve, String note);
    }

    public interface TaskService {
        Task assign(Task t);
        void close(String taskId);
    }
}
