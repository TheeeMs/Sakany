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
        Instant startDate,
        Instant endDate,
        String imageUrl,
        String hostName,
        Double price,
        Integer maxAttendees,
        String category,
        String hostRole,
        String contactPhone,
        Double latitude,
        Double longitude,
        int currentAttendees,
        EventStatus status,
        UUID approvedBy,
        Instant createdAt,
        Instant updatedAt
) {}
