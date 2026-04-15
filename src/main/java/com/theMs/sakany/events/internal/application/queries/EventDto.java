package com.theMs.sakany.events.internal.application.queries;

import com.theMs.sakany.events.internal.domain.EventStatus;

import java.time.Instant;
import java.util.UUID;

public record EventDto(
    UUID id,
    UUID organizerId,
    String title,
    String description,
    String location,
    Instant eventDate,
    Integer maxAttendees,
    int currentAttendees,
    EventStatus status,
    UUID approvedBy,
    Instant createdAt,
    Instant updatedAt
) {}
