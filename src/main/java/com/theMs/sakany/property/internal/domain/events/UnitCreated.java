package com.theMs.sakany.property.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record UnitCreated(
    UUID unitId,
    UUID buildingId,
    String unitNumber,
    Instant occurredAt
) implements DomainEvent {
    public UnitCreated {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
