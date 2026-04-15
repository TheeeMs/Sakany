package com.theMs.sakany.access.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccessCodeUsed(
    UUID accessCodeId,
    UUID residentId,
    Instant usedAt,
    Instant occurredAt
) implements DomainEvent {
}
