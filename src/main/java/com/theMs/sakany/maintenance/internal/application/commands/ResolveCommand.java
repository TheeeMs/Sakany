package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record ResolveCommand(
        UUID requestId,
        String resolutionNotes,
        BigDecimal resolutionCost
) implements Command<Void> {
}
