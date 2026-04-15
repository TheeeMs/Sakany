package com.theMs.sakany.notifications.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DeviceTokenDeactivated(
        UUID tokenId,
        UUID userId,
        Instant occurredAt
) implements DomainEvent {
    public DeviceTokenDeactivated(UUID tokenId, UUID userId) {
        this(tokenId, userId, Instant.now());
    }
}
