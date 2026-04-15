package com.theMs.sakany.accounts.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserDeactivated(
        UUID userId,
        Instant occurredAt
) implements DomainEvent {
    public UserDeactivated(UUID userId) {
        this(userId, Instant.now());
    }
}
