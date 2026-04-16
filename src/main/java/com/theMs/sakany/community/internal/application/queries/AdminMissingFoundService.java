package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundCategoryOptionRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundSummaryRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AlertJpaRepository;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminMissingFoundService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final AlertJpaRepository alertJpaRepository;
    private final AlertRepository alertRepository;

    public AdminMissingFoundService(AlertJpaRepository alertJpaRepository, AlertRepository alertRepository) {
        this.alertJpaRepository = alertJpaRepository;
        this.alertRepository = alertRepository;
    }

    public MissingFoundDashboardResponse getDashboard(
            String search,
            String type,
            String status,
            String category,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String normalizedSearch = normalize(search);
        String normalizedType = normalizeType(type);
        String normalizedStatus = normalizeStatus(status);
        String normalizedCategory = normalizeCategory(category);

        Page<AdminMissingFoundRow> rows = alertJpaRepository.findMissingFoundReports(
                normalizedSearch,
                normalizedType,
                normalizedStatus,
                normalizedCategory,
                pageable
        );

        AdminMissingFoundSummaryRow summary = alertJpaRepository.getMissingFoundSummary(normalizedSearch, normalizedCategory);

        List<MissingFoundItem> items = rows.getContent().stream().map(this::mapRow).toList();

        return new MissingFoundDashboardResponse(
                normalizedType == null ? "ALL" : normalizedType,
                normalizedStatus == null ? "ALL" : normalizedStatus,
                normalizedCategory,
                items,
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious(),
                new MissingFoundSummary(
                        summary == null ? rows.getTotalElements() : safeLong(summary.getTotalCount()),
                        summary == null ? countByType(items, "MISSING") : safeLong(summary.getMissingCount()),
                        summary == null ? countByType(items, "FOUND") : safeLong(summary.getFoundCount()),
                        summary == null ? countByStatus(items, "OPEN") : safeLong(summary.getOpenCount()),
                        summary == null ? countByStatus(items, "MATCHED") : safeLong(summary.getMatchedCount()),
                        summary == null ? countByStatus(items, "RESOLVED") : safeLong(summary.getResolvedCount())
                )
        );
    }

    public MissingFoundItem getReport(UUID reportId) {
        AdminMissingFoundRow row = alertJpaRepository.findMissingFoundReportById(reportId)
                .orElseThrow(() -> new NotFoundException("Alert", reportId));
        return mapRow(row);
    }

    public List<MissingFoundHistoryItem> getReportHistory(UUID reportId) {
        MissingFoundItem report = getReport(reportId);

        List<MissingFoundHistoryItem> history = new ArrayList<>();
        if (report.createdAt() != null) {
            history.add(new MissingFoundHistoryItem(
                    "REPORTED",
                    "Report created",
                    report.createdAt(),
                    report.reporterName(),
                    null
            ));
        }

        if (report.updatedAt() != null && report.createdAt() != null && report.updatedAt().isAfter(report.createdAt().plusSeconds(1))) {
            history.add(new MissingFoundHistoryItem(
                    "UPDATED",
                    "Report details updated",
                    report.updatedAt(),
                    "Admin",
                    null
            ));
        }

        if ("MATCHED".equalsIgnoreCase(report.status())) {
            history.add(new MissingFoundHistoryItem(
                    "MATCHED",
                    "Matching item/report identified",
                    report.updatedAt() != null ? report.updatedAt() : report.createdAt(),
                    "Admin",
                    null
            ));
        }

        if ("RESOLVED".equalsIgnoreCase(report.status()) || report.resolvedAt() != null) {
            history.add(new MissingFoundHistoryItem(
                    "RESOLVED",
                    "Case resolved",
                    report.resolvedAt() != null ? report.resolvedAt() : report.updatedAt(),
                    "Admin",
                    null
            ));
        }

        return history.stream()
                .sorted(Comparator.comparing(MissingFoundHistoryItem::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Transactional
    public void updateReport(
            UUID reportId,
            String title,
            String description,
            String location,
            AlertCategory category,
            Instant eventTime,
            List<String> photoUrls
    ) {
        Alert alert = alertRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Alert", reportId));

        Alert updated = Alert.reconstitute(
                alert.getId(),
                alert.getReporterId(),
                alert.getType(),
                category != null ? category : alert.getCategory(),
                alert.getStatus(),
                title != null && !title.isBlank() ? title.trim() : alert.getTitle(),
                description != null && !description.isBlank() ? description.trim() : alert.getDescription(),
                location != null ? location.trim() : alert.getLocation(),
                eventTime != null ? eventTime : alert.getEventTime(),
                photoUrls != null ? photoUrls : alert.getPhotoUrls(),
                alert.isResolved(),
                alert.getResolvedAt()
        );

        alertRepository.save(updated);
    }

    @Transactional
    public void updateReportStatus(UUID reportId, AlertReportStatus newStatus) {
        Alert alert = alertRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Alert", reportId));

        if (newStatus == null) {
            return;
        }

        switch (newStatus) {
            case OPEN -> alert.reopen();
            case MATCHED -> alert.markMatched();
            case RESOLVED -> alert.resolve();
        }

        alertRepository.save(alert);
    }

    @Transactional
    public void deleteReport(UUID reportId) {
        if (!alertJpaRepository.existsById(reportId)) {
            throw new NotFoundException("Alert", reportId);
        }
        alertJpaRepository.deleteById(reportId);
    }

    public List<String> getTypeOptions() {
        return List.of("ALL", "MISSING", "FOUND");
    }

    public List<String> getStatusOptions() {
        return List.of("ALL", "OPEN", "MATCHED", "RESOLVED");
    }

    public List<String> getCategoryOptions() {
        List<String> dbOptions = alertJpaRepository.findMissingFoundCategoryOptions().stream()
                .map(AdminMissingFoundCategoryOptionRow::getCategory)
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .toList();

        List<String> defaults = Arrays.stream(AlertCategory.values()).map(Enum::name).toList();
        return java.util.stream.Stream.concat(defaults.stream(), dbOptions.stream())
                .distinct()
                .toList();
    }

    private MissingFoundItem mapRow(AdminMissingFoundRow row) {
        String type = row.getType() == null ? "OTHER" : row.getType();
        String status = normalizeDashboardStatus(row.getStatus(), row.getResolved());
        String reporterName = buildFullName(row.getReporterFirstName(), row.getReporterLastName());
        String reporterUnit = buildReporterUnit(row.getReporterBuildingName(), row.getReporterUnitNumber());

        UUID reportId = row.getReportId();

        return new MissingFoundItem(
                reportId,
                type,
                row.getCategory(),
                status,
                row.getTitle(),
                row.getDescription(),
                row.getLocation(),
                row.getEventTime(),
                row.getCreatedAt(),
                row.getUpdatedAt(),
                row.getResolvedAt(),
                row.getReporterId(),
                reporterName,
                reporterUnit,
                true,
                true,
                true,
                "/v1/admin/missing-found/reports/" + reportId,
                "/v1/admin/missing-found/reports/" + reportId,
                "/v1/admin/missing-found/reports/" + reportId,
                "/v1/admin/missing-found/reports/" + reportId + "/history",
                "/v1/admin/missing-found/reports/" + reportId + "/status"
        );
    }

    private String normalizeType(String value) {
        String normalized = normalizeEnumLike(value);
        if (normalized == null || "ALL".equals(normalized)) {
            return null;
        }
        if (!"MISSING".equals(normalized) && !"FOUND".equals(normalized)) {
            return null;
        }
        return normalized;
    }

    private String normalizeStatus(String value) {
        String normalized = normalizeEnumLike(value);
        if (normalized == null || "ALL".equals(normalized)) {
            return null;
        }
        if (!"OPEN".equals(normalized) && !"MATCHED".equals(normalized) && !"RESOLVED".equals(normalized)) {
            return null;
        }
        return normalized;
    }

    private String normalizeCategory(String value) {
        String normalized = normalizeEnumLike(value);
        if (normalized == null || "ALL".equals(normalized)) {
            return null;
        }

        boolean known = Arrays.stream(AlertCategory.values()).anyMatch(candidate -> candidate.name().equals(normalized));
        return known ? normalized : null;
    }

    private String normalizeDashboardStatus(String status, Boolean resolved) {
        if (status != null && !status.isBlank()) {
            String normalized = status.trim().toUpperCase(Locale.ROOT);
            if ("OPEN".equals(normalized) || "MATCHED".equals(normalized) || "RESOLVED".equals(normalized)) {
                return normalized;
            }
        }

        return Boolean.TRUE.equals(resolved) ? "RESOLVED" : "OPEN";
    }

    private String normalizeEnumLike(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildFullName(String firstName, String lastName) {
        String full = ((firstName == null ? "" : firstName.trim()) + " "
                + (lastName == null ? "" : lastName.trim())).trim();
        return full.isBlank() ? "Unknown Reporter" : full;
    }

    private String buildReporterUnit(String buildingName, String unitNumber) {
        String unit = unitNumber == null || unitNumber.isBlank() ? null : unitNumber.trim();
        String building = buildingName == null || buildingName.isBlank() ? null : buildingName.trim();

        if (building != null && unit != null) {
            return building + " - " + unit;
        }
        if (unit != null) {
            return unit;
        }
        if (building != null) {
            return building;
        }
        return "N/A";
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private long countByType(List<MissingFoundItem> items, String type) {
        return items.stream().filter(item -> type.equalsIgnoreCase(item.type())).count();
    }

    private long countByStatus(List<MissingFoundItem> items, String status) {
        return items.stream().filter(item -> status.equalsIgnoreCase(item.status())).count();
    }

    public record MissingFoundDashboardResponse(
            String selectedType,
            String selectedStatus,
            String selectedCategory,
            List<MissingFoundItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            MissingFoundSummary summary
    ) {
    }

    public record MissingFoundSummary(
            long totalCount,
            long missingCount,
            long foundCount,
            long openCount,
            long matchedCount,
            long resolvedCount
    ) {
    }

    public record MissingFoundItem(
            UUID reportId,
            String type,
            String category,
            String status,
            String title,
            String description,
            String location,
            Instant eventTime,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt,
            UUID reporterId,
            String reporterName,
            String reporterUnit,
            boolean canView,
            boolean canEdit,
            boolean canDelete,
            String viewUrl,
            String editUrl,
            String deleteUrl,
                String historyUrl,
            String statusUrl
    ) {
    }

            public record MissingFoundHistoryItem(
                String key,
                String label,
                Instant occurredAt,
                String actor,
                String details
            ) {
            }
}
