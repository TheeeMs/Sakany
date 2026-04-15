package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.EventStatus;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "community_events")
public class CommunityEventEntity extends BaseEntity {
    private UUID organizerId;
    private String title;
    private String description;
    private String location;
    private Instant eventDate;
    private Integer maxAttendees;
    private int currentAttendees;

    @Enumerated(EnumType.STRING)
    private EventStatus status;
    private UUID approvedBy;

    // Default constructor for JPA
    protected CommunityEventEntity() {}

    public CommunityEventEntity(UUID id, UUID organizerId, String title, String description, String location,
                                Instant eventDate, Integer maxAttendees, int currentAttendees, EventStatus status, UUID approvedBy) {
        try {
            java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(this, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.maxAttendees = maxAttendees;
        this.currentAttendees = currentAttendees;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public UUID getOrganizerId() { return organizerId; }
    public void setOrganizerId(UUID organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Instant getEventDate() { return eventDate; }
    public void setEventDate(Instant eventDate) { this.eventDate = eventDate; }

    public Integer getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(Integer maxAttendees) { this.maxAttendees = maxAttendees; }

    public int getCurrentAttendees() { return currentAttendees; }
    public void setCurrentAttendees(int currentAttendees) { this.currentAttendees = currentAttendees; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
}
