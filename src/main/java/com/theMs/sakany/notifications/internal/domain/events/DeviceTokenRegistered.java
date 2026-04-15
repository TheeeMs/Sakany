package com.theMs.sakany.notifications.internal.domain.events;

import com.theMs.sakany.notifications.internal.domain.Platform;
import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DeviceTokenRegistered(
        UUID tokenId,
        UUID userId,
        String token,
        Platform platform,
        Instant occurredAt
) implements DomainEvent {
    public DeviceTokenRegistered(
            UUID tokenId,
            UUID userId,
            String token,
            Platform platform
    ) {
        this(tokenId, userId, token, platform, Instant.now());
    }
}
