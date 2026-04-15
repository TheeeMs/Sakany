package com.theMs.sakany.access.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccessCodeRevoked(
    UUID accessCodeId,
    UUID residentId,
    Instant occurredAt
) implements DomainEvent {
}
