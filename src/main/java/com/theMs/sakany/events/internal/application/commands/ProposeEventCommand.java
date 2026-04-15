package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.time.Instant;
import java.util.UUID;

public record ProposeEventCommand(
    UUID organizerId,
    String title,
    String description,
    String location,
    Instant eventDate,
    Integer maxAttendees
) implements Command<UUID> {}
