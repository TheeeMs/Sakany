package com.theMs.sakany.notifications.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record NotificationSent(
        UUID notificationId,
        UUID recipientId,
        Instant sentAt,
        Instant occurredAt
) implements DomainEvent {
    public NotificationSent(UUID notificationId, UUID recipientId, Instant sentAt) {
        this(notificationId, recipientId, sentAt, Instant.now());
    }
}
