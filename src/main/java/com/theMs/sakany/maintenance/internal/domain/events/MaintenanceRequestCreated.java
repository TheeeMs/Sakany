package com.theMs.sakany.maintenance.internal.domain.events;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceCategory;
import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;
import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MaintenanceRequestCreated(
    UUID requestId,
    UUID residentId,
    UUID unitId,
    String title,
    MaintenanceCategory category,
    MaintenancePriority priority,
    Instant occurredAt
) implements DomainEvent {
}
