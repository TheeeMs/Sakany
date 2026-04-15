package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceCategory;
import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.List;
import java.util.UUID;

public record CreateMaintenanceRequestCommand(
        UUID residentId,
        UUID unitId,
        String title,
        String description,
        MaintenanceCategory category,
        MaintenancePriority priority,
        boolean isPublic,
        List<String> photoUrls
) implements Command<UUID> {
}
