package com.theMs.sakany.maintenance.internal.domain;

import com.theMs.sakany.maintenance.internal.domain.events.*;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MaintenanceRequest extends AggregateRoot {
    private UUID id;
    private UUID residentId;
    private UUID unitId;
    private UUID technicianId;
    private String title;
    private String description;
    private MaintenanceCategory category;
    private MaintenancePriority priority;
    private MaintenanceStatus status;
    private boolean isPublic;
    private List<String> photoUrls;
    private Instant resolvedAt;
    private Instant createdAt;

    private MaintenanceRequest(UUID id, UUID residentId, UUID unitId, UUID technicianId, String title,
                               String description, MaintenanceCategory category, MaintenancePriority priority,
                               MaintenanceStatus status, boolean isPublic, List<String> photoUrls, Instant resolvedAt, Instant createdAt) {
        this.id = id;
        this.residentId = residentId;
        this.unitId = unitId;
        this.technicianId = technicianId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.isPublic = isPublic;
        this.photoUrls = photoUrls;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
    }
    
    // For mapping from persistence
    public static MaintenanceRequest reconstitute(UUID id, UUID residentId, UUID unitId, UUID technicianId, String title,
                                                  String description, MaintenanceCategory category, MaintenancePriority priority,
                                                  MaintenanceStatus status, boolean isPublic, List<String> photoUrls, Instant resolvedAt, Instant createdAt) {
        return new MaintenanceRequest(id, residentId, unitId, technicianId, title, description, category, priority, status, isPublic, photoUrls, resolvedAt, createdAt);
    }

    public static MaintenanceRequest create(UUID residentId, UUID unitId, String title, String description,
                                            MaintenanceCategory category, MaintenancePriority priority,
                                            boolean isPublic, List<String> photoUrls) {
        if (residentId == null) throw new BusinessRuleException("Resident ID is required");
        if (unitId == null) throw new BusinessRuleException("Unit ID is required");
        if (title == null || title.trim().isEmpty()) throw new BusinessRuleException("Title is required");
        if (description == null || description.trim().isEmpty()) throw new BusinessRuleException("Description is required");
        if (category == null) throw new BusinessRuleException("Category is required");

        MaintenancePriority effectivePriority = (priority != null) ? priority : MaintenancePriority.NORMAL;

        UUID id = UUID.randomUUID();
        MaintenanceRequest request = new MaintenanceRequest(
                id, residentId, unitId, null, title, description, category, effectivePriority,
                MaintenanceStatus.SUBMITTED, isPublic, 
                photoUrls == null ? new ArrayList<>() : new ArrayList<>(photoUrls), 
                null, Instant.now()
        );

        request.registerEvent(new MaintenanceRequestCreated(
                id, residentId, unitId, title, category, effectivePriority, Instant.now()
        ));

        return request;
    }

    private void ensureNotTerminal() {
        if (this.status == MaintenanceStatus.RESOLVED || 
            this.status == MaintenanceStatus.CANCELLED || 
            this.status == MaintenanceStatus.REJECTED) {
            throw new BusinessRuleException("Cannot modify a maintenance request in a terminal state (" + this.status + ")");
        }
    }

    public void assign(UUID technicianId) {
        ensureNotTerminal();
        if (technicianId == null) {
            throw new BusinessRuleException("Technician ID must be provided to assign");
        }
        
        this.technicianId = technicianId;
        this.status = MaintenanceStatus.ASSIGNED;
        
        this.registerEvent(new MaintenanceRequestAssigned(this.id, this.technicianId, Instant.now()));
    }

    public void startWork() {
        ensureNotTerminal();
        if (this.status != MaintenanceStatus.ASSIGNED) {
            throw new BusinessRuleException("Maintenance request must be in ASSIGNED status to start work");
        }
        
        this.status = MaintenanceStatus.IN_PROGRESS;
        
        this.registerEvent(new MaintenanceRequestStarted(this.id, Instant.now()));
    }

    public void resolve() {
        ensureNotTerminal();
        if (this.technicianId == null) {
            throw new BusinessRuleException("Cannot resolve a maintenance request without an assigned technician");
        }
        
        this.status = MaintenanceStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        
        this.registerEvent(new MaintenanceRequestResolved(this.id, this.technicianId, this.resolvedAt));
    }

    public void cancel() {
        ensureNotTerminal();
        if (this.status != MaintenanceStatus.SUBMITTED && this.status != MaintenanceStatus.ASSIGNED) {
            throw new BusinessRuleException("Can only cancel maintenance requests that are SUBMITTED or ASSIGNED");
        }
        
        this.status = MaintenanceStatus.CANCELLED;
        
        this.registerEvent(new MaintenanceRequestCancelled(this.id, Instant.now()));
    }

    public void reject(String reason) {
        ensureNotTerminal();
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Rejection reason is required");
        }
        
        this.status = MaintenanceStatus.REJECTED;
        
        this.registerEvent(new MaintenanceRequestRejected(this.id, reason, Instant.now()));
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getResidentId() { return residentId; }
    public UUID getUnitId() { return unitId; }
    public UUID getTechnicianId() { return technicianId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MaintenanceCategory getCategory() { return category; }
    public MaintenancePriority getPriority() { return priority; }
    public MaintenanceStatus getStatus() { return status; }
    public boolean isPublic() { return isPublic; }
    public List<String> getPhotoUrls() { return new ArrayList<>(photoUrls); }
    public Instant getResolvedAt() { return resolvedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
