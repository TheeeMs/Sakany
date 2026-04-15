package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import com.theMs.sakany.community.internal.domain.AlertType;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record AlertCreated(
    UUID alertId,
    UUID reporterId,
    AlertType type,
    String title,
    String description,
    List<String> photoUrls,
    Instant occurredAt
) implements DomainEvent {
    public AlertCreated {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
