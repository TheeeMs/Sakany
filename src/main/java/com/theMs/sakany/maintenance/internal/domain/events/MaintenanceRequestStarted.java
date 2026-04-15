package com.theMs.sakany.maintenance.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MaintenanceRequestStarted(
    UUID requestId,
    Instant occurredAt
) implements DomainEvent {
}
