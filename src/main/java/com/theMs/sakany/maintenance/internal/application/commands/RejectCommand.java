package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record RejectCommand(
        UUID requestId,
        String reason
) implements Command<Void> {
}
