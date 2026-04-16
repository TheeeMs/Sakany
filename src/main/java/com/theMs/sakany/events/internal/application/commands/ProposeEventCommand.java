package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.time.Instant;
import java.util.UUID;

public record ProposeEventCommand(
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
        Double longitude
) implements Command<UUID> {
}
