package com.theMs.sakany.access.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record VisitorEntryLogged(
    UUID visitLogId,
    UUID accessCodeId,
    UUID residentId,
    String visitorName,
    Instant entryTime,
    Instant occurredAt
) implements DomainEvent {
}
