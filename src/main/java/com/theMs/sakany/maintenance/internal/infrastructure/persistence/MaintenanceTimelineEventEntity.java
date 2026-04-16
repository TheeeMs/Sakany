package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "maintenance_timeline_events")
public class MaintenanceTimelineEventEntity extends BaseEntity {

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "details")
    private String details;

    @Column(name = "actor_id")
    private UUID actorId;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }
}
