package com.theMs.sakany.notifications.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record NotificationFailed(
        UUID notificationId,
        UUID recipientId,
        String reason,
        Instant occurredAt
) implements DomainEvent {
    public NotificationFailed(UUID notificationId, UUID recipientId, String reason) {
        this(notificationId, recipientId, reason, Instant.now());
    }
}
