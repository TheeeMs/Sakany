package com.theMs.sakany.events.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RegistrationCancelled(
        UUID registrationId,
        UUID eventId,
        UUID residentId,
        Instant occurredAt
) implements DomainEvent {
    public RegistrationCancelled(UUID registrationId, UUID eventId, UUID residentId) {
        this(registrationId, eventId, residentId, Instant.now());
    }
}
