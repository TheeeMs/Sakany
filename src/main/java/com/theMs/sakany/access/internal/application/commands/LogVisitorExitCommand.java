package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record LogVisitorExitCommand(
    UUID visitLogId
) implements Command {
}
