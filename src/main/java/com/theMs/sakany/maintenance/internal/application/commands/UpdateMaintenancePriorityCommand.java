package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record UpdateMaintenancePriorityCommand(
        UUID requestId,
        MaintenancePriority priority
) implements Command<Void> {
}
