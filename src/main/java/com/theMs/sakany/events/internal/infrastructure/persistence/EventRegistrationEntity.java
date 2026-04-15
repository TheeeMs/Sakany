package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.RegistrationStatus;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_registrations")
public class EventRegistrationEntity extends BaseEntity {
    private UUID eventId;
    private UUID residentId;
    private Instant registeredAt;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    protected EventRegistrationEntity() {}

    public EventRegistrationEntity(UUID id, UUID eventId, UUID residentId, Instant registeredAt, RegistrationStatus status) {
        try {
            java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(this, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.eventId = eventId;
        this.residentId = residentId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getResidentId() { return residentId; }
    public void setResidentId(UUID residentId) { this.residentId = residentId; }

    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }

    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
}
