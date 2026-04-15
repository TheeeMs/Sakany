package com.theMs.sakany.events.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EventCancelled(
        UUID eventId,
        Instant occurredAt
) implements DomainEvent {
    public EventCancelled(UUID eventId) {
        this(eventId, Instant.now());
    }
}
