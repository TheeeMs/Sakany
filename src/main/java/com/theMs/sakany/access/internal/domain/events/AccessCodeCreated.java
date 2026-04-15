package com.theMs.sakany.access.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AccessCodeCreated(
    UUID accessCodeId,
    UUID residentId,
    String visitorName,
    String code,
    Instant validFrom,
    Instant validUntil,
    Instant occurredAt
) implements DomainEvent {
}
