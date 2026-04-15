package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import com.theMs.sakany.community.internal.domain.FeedbackStatus;

import java.time.Instant;
import java.util.UUID;

public record FeedbackStatusChanged(
    UUID feedbackId,
    FeedbackStatus oldStatus,
    FeedbackStatus newStatus,
    Instant occurredAt
) implements DomainEvent {
    public FeedbackStatusChanged {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
