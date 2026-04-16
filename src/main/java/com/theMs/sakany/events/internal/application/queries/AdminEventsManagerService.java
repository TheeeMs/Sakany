package com.theMs.sakany.events.internal.application.queries;

import com.theMs.sakany.events.internal.infrastructure.persistence.AdminEventCategoryOptionRow;
import com.theMs.sakany.events.internal.infrastructure.persistence.AdminEventManagerJpaRepository;
import com.theMs.sakany.events.internal.infrastructure.persistence.AdminEventManagerRow;
import com.theMs.sakany.events.internal.infrastructure.persistence.AdminEventSummaryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminEventsManagerService {

    private static final int DEFAULT_PAGE_SIZE = 9;
    private static final int MAX_PAGE_SIZE = 100;
    private static final List<String> DEFAULT_EVENT_CATEGORIES = List.of(
            "Social",
            "Sports",
            "Health & Wellness",
            "Entertainment",
            "Cultural",
            "Educational"
    );

    private final AdminEventManagerJpaRepository adminEventManagerJpaRepository;

    public AdminEventsManagerService(AdminEventManagerJpaRepository adminEventManagerJpaRepository) {
        this.adminEventManagerJpaRepository = adminEventManagerJpaRepository;
    }

    public AdminEventsDashboardResponse getDashboard(
            String search,
            AdminEventStatusFilter status,
            String category,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String normalizedSearch = normalize(search);
        String normalizedCategory = normalize(category);
        String statusFilter = status == null ? AdminEventStatusFilter.ALL.name() : status.name();

        Page<AdminEventManagerRow> rows = adminEventManagerJpaRepository.findForDashboard(
                normalizedSearch,
                statusFilter,
                normalizedCategory,
                pageable
        );

        AdminEventSummaryRow summary = adminEventManagerJpaRepository.getDashboardSummary();
        AdminEventManagerRow pendingEvent = adminEventManagerJpaRepository.findTopPendingApproval().orElse(null);

        return new AdminEventsDashboardResponse(
                rows.getContent().stream().map(this::mapRow).toList(),
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious(),
                new AdminEventsSummary(
                        summary != null ? safeLong(summary.getTotalCount()) : rows.getTotalElements(),
                        summary != null ? safeLong(summary.getPendingCount()) : 0L,
                        summary != null ? safeLong(summary.getApprovedCount()) : 0L,
                        summary != null ? safeLong(summary.getOngoingCount()) : 0L,
                        summary != null ? safeLong(summary.getCompletedCount()) : 0L,
                        summary != null ? safeLong(summary.getRejectedCount()) : 0L
                ),
                new PendingApprovalPanel(
                        summary != null ? safeLong(summary.getPendingCount()) : 0L,
                        pendingEvent == null ? null : mapRow(pendingEvent)
                )
        );
    }

    public List<String> getCategoryOptions() {
        LinkedHashSet<String> categories = new LinkedHashSet<>(DEFAULT_EVENT_CATEGORIES);
        categories.addAll(adminEventManagerJpaRepository.findCategoryOptions().stream()
                .map(AdminEventCategoryOptionRow::getCategory)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList());
        return List.copyOf(categories);
    }

    public List<String> getStatusOptions() {
        return Arrays.stream(AdminEventStatusFilter.values()).map(Enum::name).toList();
    }

    private AdminEventCardItem mapRow(AdminEventManagerRow row) {
        int currentAttendees = row.getCurrentAttendees() == null ? 0 : row.getCurrentAttendees();
        int maxAttendees = row.getMaxAttendees() == null ? 0 : row.getMaxAttendees();
        int occupancyPercent = maxAttendees > 0
                ? (int) Math.round((currentAttendees * 100.0) / maxAttendees)
                : 0;

        return new AdminEventCardItem(
                row.getEventId(),
                row.getTitle(),
                row.getDescription(),
                row.getLocation(),
                row.getStartDate(),
                row.getEndDate(),
                row.getImageUrl(),
                row.getCategory(),
                row.getWorkflowStatus(),
                row.getUiStatus(),
                row.getOrganizerId(),
                resolveOrganizerName(row),
                row.getHostName(),
                currentAttendees,
                row.getMaxAttendees(),
                occupancyPercent,
                false,
                row.getCreatedAt(),
                "PENDING".equalsIgnoreCase(row.getUiStatus()),
                "PENDING".equalsIgnoreCase(row.getUiStatus())
        );
    }

    private String resolveOrganizerName(AdminEventManagerRow row) {
        String organizer = ((row.getOrganizerFirstName() == null ? "" : row.getOrganizerFirstName())
                + " "
                + (row.getOrganizerLastName() == null ? "" : row.getOrganizerLastName())).trim();
        if (!organizer.isEmpty()) {
            return organizer;
        }
        if (row.getHostName() != null && !row.getHostName().isBlank()) {
            return row.getHostName();
        }
        return "Unknown Organizer";
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    public record AdminEventsDashboardResponse(
            List<AdminEventCardItem> events,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            AdminEventsSummary summary,
            PendingApprovalPanel pendingApproval
    ) {
    }

    public record AdminEventCardItem(
            UUID eventId,
            String title,
            String description,
            String location,
            Instant startDate,
            Instant endDate,
            String imageUrl,
            String category,
            String workflowStatus,
            String uiStatus,
            UUID organizerId,
            String organizerName,
            String hostName,
            int currentAttendees,
            Integer maxAttendees,
            int occupancyPercent,
            boolean recurring,
            Instant createdAt,
            boolean canApprove,
            boolean canReject
    ) {
    }

    public record AdminEventsSummary(
            long totalCount,
            long pendingCount,
            long approvedCount,
            long ongoingCount,
            long completedCount,
            long rejectedCount
    ) {
    }

    public record PendingApprovalPanel(
            long pendingCount,
            AdminEventCardItem topPendingEvent
    ) {
    }
}
