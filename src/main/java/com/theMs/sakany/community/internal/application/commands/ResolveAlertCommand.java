package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record ResolveAlertCommand(
    UUID alertId,
    UUID requestingUserId
) implements Command<Void> {}
