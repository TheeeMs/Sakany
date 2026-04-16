package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.domain.AlertCategory;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record AlertCreated(
    UUID alertId,
    UUID reporterId,
    AlertType type,
    AlertCategory category,
    String title,
    String description,
    String location,
    Instant eventTime,
    List<String> photoUrls,
    Instant occurredAt
) implements DomainEvent {
    public AlertCreated {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
