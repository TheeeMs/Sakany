package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import com.theMs.sakany.community.internal.domain.VoteType;

import java.time.Instant;
import java.util.UUID;

public record FeedbackVoted(
    UUID feedbackId,
    UUID voterId,
    VoteType voteType,
    Instant occurredAt
) implements DomainEvent {
    public FeedbackVoted {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
