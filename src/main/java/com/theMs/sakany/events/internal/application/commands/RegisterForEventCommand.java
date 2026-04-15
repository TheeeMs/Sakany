package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record RegisterForEventCommand(
    UUID eventId,
    UUID residentId
) implements Command<UUID> {}
