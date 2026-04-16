package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.time.Instant;
import java.util.UUID;

public record ReactivateAccessCodeCommand(
    UUID residentId,
    UUID existingAccessCodeId,
    Instant validFrom,
    Instant validUntil
) implements Command {
}