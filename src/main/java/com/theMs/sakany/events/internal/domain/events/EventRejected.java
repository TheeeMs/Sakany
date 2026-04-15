package com.theMs.sakany.events.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EventRejected(
        UUID eventId,
        Instant occurredAt
) implements DomainEvent {
    public EventRejected(UUID eventId) {
        this(eventId, Instant.now());
    }
}
