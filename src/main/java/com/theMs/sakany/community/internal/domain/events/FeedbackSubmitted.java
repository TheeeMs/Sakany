package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import com.theMs.sakany.community.internal.domain.FeedbackType;

import java.time.Instant;
import java.util.UUID;

public record FeedbackSubmitted(
    UUID feedbackId,
    UUID authorId,
    String title,
    String content,
    FeedbackType type,
    boolean isPublic,
    Instant occurredAt
) implements DomainEvent {
    public FeedbackSubmitted {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
