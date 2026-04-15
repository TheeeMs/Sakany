package com.theMs.sakany.community.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;
import com.theMs.sakany.community.internal.domain.AnnouncementPriority;

import java.time.Instant;
import java.util.UUID;

public record AnnouncementPublished(
    UUID announcementId,
    UUID authorId,
    String title,
    String content,
    AnnouncementPriority priority,
    Instant expiresAt,
    Instant occurredAt
) implements DomainEvent {
    public AnnouncementPublished {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
