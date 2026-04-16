package com.theMs.sakany.maintenance.internal.api.dto;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceCategory;
import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MaintenanceRequestResponseDto(
        UUID id,
        UUID residentId,
        UUID unitId,
        UUID technicianId,
        String title,
        String description,
        String locationLabel,
        MaintenanceCategory category,
        MaintenancePriority priority,
        MaintenanceStatus status,
        boolean isPublic,
        List<String> photoUrls,
        Instant resolvedAt,
        String resolutionNotes,
        BigDecimal resolutionCost,
        Instant createdAt
) {
    public static MaintenanceRequestResponseDto fromDomain(MaintenanceRequest request) {
        return new MaintenanceRequestResponseDto(
                request.getId(),
                request.getResidentId(),
                request.getUnitId(),
                request.getTechnicianId(),
                request.getTitle(),
                request.getDescription(),
                request.getLocationLabel(),
                request.getCategory(),
                request.getPriority(),
                request.getStatus(),
                request.isPublic(),
                request.getPhotoUrls(),
                request.getResolvedAt(),
                request.getResolutionNotes(),
                request.getResolutionCost(),
                request.getCreatedAt()
        );
    }
}
