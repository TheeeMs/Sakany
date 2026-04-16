package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceCategory;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.AdminMaintenanceAreaOptionRow;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.AdminMaintenanceCommandCenterRow;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.AdminMaintenanceCommandCenterSummaryRow;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceRequestJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminMaintenanceCommandCenterService {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;

    private final MaintenanceRequestJpaRepository maintenanceRequestJpaRepository;

    public AdminMaintenanceCommandCenterService(MaintenanceRequestJpaRepository maintenanceRequestJpaRepository) {
        this.maintenanceRequestJpaRepository = maintenanceRequestJpaRepository;
    }

    public MaintenanceCommandCenterPage getCommandCenter(
            AdminMaintenanceTab tab,
            String area,
            AdminMaintenanceRequestType requestType,
            MaintenanceCategory category,
            AdminMaintenanceSortBy sortBy,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<AdminMaintenanceCommandCenterRow> rows = maintenanceRequestJpaRepository.findForCommandCenter(
                tab != null ? tab.name() : null,
                normalize(area),
                requestType != null ? requestType.name() : null,
                category != null ? category.name() : null,
                sortBy != null ? sortBy.name() : AdminMaintenanceSortBy.NEWEST.name(),
                pageable
        );

        AdminMaintenanceCommandCenterSummaryRow summary = maintenanceRequestJpaRepository.getCommandCenterSummary(
                normalize(area),
                requestType != null ? requestType.name() : null,
                category != null ? category.name() : null
        );

        List<MaintenanceCommandCenterItem> items = rows.getContent().stream()
                .map(this::mapRow)
                .toList();

        return new MaintenanceCommandCenterPage(
                items,
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious(),
                summary != null ? summary.getTotalCount() : rows.getTotalElements(),
                summary != null ? summary.getPendingCount() : 0L,
                summary != null ? summary.getInProgressCount() : 0L,
                summary != null ? summary.getCompletedCount() : 0L
        );
    }

    public List<String> getAreaOptions() {
        return maintenanceRequestJpaRepository.findDistinctAreaOptions().stream()
                .map(AdminMaintenanceAreaOptionRow::getAreaLabel)
                .filter(a -> a != null && !a.isBlank())
                .toList();
    }

    private MaintenanceCommandCenterItem mapRow(AdminMaintenanceCommandCenterRow row) {
        String requestType = Boolean.TRUE.equals(row.getPublicRequest()) ? "PUBLIC" : "PRIVATE";
        String priority = mapPriority(row.getPriority());
        String statusBadge = mapStatusBadge(row.getWorkflowStatus(), row.getTechnicianId());

        return new MaintenanceCommandCenterItem(
                row.getRequestId(),
                formatDisplayId(row.getRequestId(), row.getRequestedAt()),
                requestType,
                priority,
                row.getIssueTitle(),
                row.getCategory(),
                row.getLocationLabel(),
                row.getRequestedAt(),
                statusBadge,
                row.getWorkflowStatus(),
                row.getTechnicianId()
        );
    }

    private String mapPriority(String priority) {
        if (priority == null) {
            return "MEDIUM";
        }
        return switch (priority) {
            case "LOW" -> "LOW";
            case "NORMAL" -> "MEDIUM";
            case "URGENT", "EMERGENCY" -> "HIGH";
            default -> priority;
        };
    }

    private String mapStatusBadge(String workflowStatus, UUID technicianId) {
        if (workflowStatus == null) {
            return "UNASSIGNED";
        }
        return switch (workflowStatus) {
            case "RESOLVED" -> "COMPLETED";
            case "ASSIGNED", "IN_PROGRESS" -> "ASSIGNED";
            case "SUBMITTED" -> technicianId == null ? "UNASSIGNED" : "ASSIGNED";
            default -> "CLOSED";
        };
    }

    private String formatDisplayId(UUID id, Instant requestedAt) {
        int year = requestedAt != null ? requestedAt.atZone(ZoneOffset.UTC).getYear() : Instant.now().atZone(ZoneOffset.UTC).getYear();
        int sequence = Math.abs(id.hashCode()) % 1000;
        return String.format("REQ-%d-%03d", year, sequence);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record MaintenanceCommandCenterItem(
            UUID id,
            String displayId,
            String type,
            String priority,
            String issue,
            String category,
            String location,
            Instant requestedAt,
            String status,
            String workflowStatus,
            UUID technicianId
    ) {
    }

    public record MaintenanceCommandCenterPage(
            List<MaintenanceCommandCenterItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            long totalCount,
            long pendingCount,
            long inProgressCount,
            long completedCount
    ) {
    }
}
