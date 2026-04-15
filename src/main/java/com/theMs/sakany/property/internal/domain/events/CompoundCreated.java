package com.theMs.sakany.property.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record CompoundCreated(
    UUID compoundId,
    String name,
    Instant occurredAt
) implements DomainEvent {
    public CompoundCreated {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
