package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record RejectEventCommand(
    UUID eventId,
    UUID adminId
) implements Command<Void> {}
