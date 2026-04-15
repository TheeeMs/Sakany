package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AnnouncementDeactivated(
    UUID announcementId,
    Instant occurredAt
) implements DomainEvent {
    public AnnouncementDeactivated {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
