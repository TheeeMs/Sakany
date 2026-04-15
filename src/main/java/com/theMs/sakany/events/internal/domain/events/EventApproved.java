package com.theMs.sakany.events.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record EventApproved(
        UUID eventId,
        UUID approvedBy,
        Instant occurredAt
) implements DomainEvent {
    public EventApproved(UUID eventId, UUID approvedBy) {
        this(eventId, approvedBy, Instant.now());
    }
}
