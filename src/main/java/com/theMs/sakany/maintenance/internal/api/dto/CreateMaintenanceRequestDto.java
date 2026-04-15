package com.theMs.sakany.maintenance.internal.api.dto;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceCategory;
import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;

import java.util.List;
import java.util.UUID;

public record CreateMaintenanceRequestDto(
        UUID residentId,
        UUID unitId,
        String title,
        String description,
        MaintenanceCategory category,
        MaintenancePriority priority,
        boolean isPublic,
        List<String> photoUrls
) {
}
