package com.theMs.sakany.events.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EventProposed(
        UUID eventId,
        UUID organizerId,
        String title,
        Instant occurredAt
) implements DomainEvent {
    public EventProposed(UUID eventId, UUID organizerId, String title) {
        this(eventId, organizerId, title, Instant.now());
    }
}
