package com.theMs.sakany.access.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VisitorExitLogged(
    UUID visitLogId,
    UUID residentId,
    Instant exitTime,
    Instant occurredAt
) implements DomainEvent {
}
