package com.theMs.sakany.notifications.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record NotificationRead(
        UUID notificationId,
        UUID recipientId,
        Instant readAt,
        Instant occurredAt
) implements DomainEvent {
    public NotificationRead(UUID notificationId, UUID recipientId, Instant readAt) {
        this(notificationId, recipientId, readAt, Instant.now());
    }
}
