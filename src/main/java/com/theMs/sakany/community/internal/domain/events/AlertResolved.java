package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AlertResolved(
    UUID alertId,
    Instant resolvedAt,
    Instant occurredAt
) implements DomainEvent {
    public AlertResolved {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
