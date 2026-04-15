package com.theMs.sakany.events.internal.domain;

import com.theMs.sakany.events.internal.domain.events.RegistrationCancelled;
import com.theMs.sakany.events.internal.domain.events.ResidentRegistered;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class EventRegistration extends AggregateRoot {
    private final UUID id;
    private final UUID eventId;
    private final UUID residentId;
    private final Instant registeredAt;
    private RegistrationStatus status;

    // Constructor for reconstruction from persistence
    public EventRegistration(UUID id, UUID eventId, UUID residentId, Instant registeredAt, RegistrationStatus status) {
        this.id = id;
        this.eventId = eventId;
        this.residentId = residentId;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public static EventRegistration register(UUID eventId, UUID residentId) {
        UUID id = UUID.randomUUID();
        EventRegistration registration = new EventRegistration(id, eventId, residentId, Instant.now(), RegistrationStatus.REGISTERED);
        registration.registerEvent(new ResidentRegistered(id, eventId, residentId));
        return registration;
    }

    public void cancel() {
        if (this.status == RegistrationStatus.CANCELLED) {
            throw new BusinessRuleException("Registration is already cancelled");
        }
        this.status = RegistrationStatus.CANCELLED;
        this.registerEvent(new RegistrationCancelled(this.id, this.eventId, this.residentId));
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getResidentId() { return residentId; }
    public Instant getRegisteredAt() { return registeredAt; }
    public RegistrationStatus getStatus() { return status; }
}
