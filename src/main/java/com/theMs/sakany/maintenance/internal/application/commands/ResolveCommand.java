package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record ResolveCommand(
        UUID requestId
) implements Command<Void> {
}
