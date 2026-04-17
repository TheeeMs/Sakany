package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundReportRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundSummaryRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AlertJpaRepository;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminMissingFoundService {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;

    private final AlertJpaRepository alertJpaRepository;
    private final AlertRepository alertRepository;

    public AdminMissingFoundService(
            AlertJpaRepository alertJpaRepository,
            AlertRepository alertRepository
    ) {
        this.alertJpaRepository = alertJpaRepository;
        this.alertRepository = alertRepository;
    }

    public MissingFoundReportsPage getReports(
            String search,
            AdminMissingFoundTypeFilter typeFilter,
            AdminMissingFoundStatusFilter statusFilter,
            AlertCategory category,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String normalizedSearch = normalize(search);
        String typeValue = (typeFilter != null ? typeFilter : AdminMissingFoundTypeFilter.ALL).name();
        String statusValue = (statusFilter != null ? statusFilter : AdminMissingFoundStatusFilter.ALL).name();
        String categoryValue = category != null ? category.name() : null;

        Page<AdminMissingFoundReportRow> rows = alertJpaRepository.findMissingFoundReportsForAdmin(
                normalizedSearch,
                typeValue,
                statusValue,
                categoryValue,
                pageable
        );

        MissingFoundSummary summary = getSummaryInternal(normalizedSearch, typeValue, statusValue, categoryValue);
        List<MissingFoundReportItem> reports = rows.getContent().stream().map(this::mapReportRow).toList();

        return new MissingFoundReportsPage(
                reports,
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious(),
                summary.totalCount(),
                summary.missingCount(),
                summary.foundCount(),
                summary.openCount(),
                summary.matchedCount(),
                summary.resolvedCount()
        );
    }

    public MissingFoundSummary getSummary(
            String search,
            AdminMissingFoundTypeFilter typeFilter,
            AdminMissingFoundStatusFilter statusFilter,
            AlertCategory category
    ) {
        String normalizedSearch = normalize(search);
        String typeValue = (typeFilter != null ? typeFilter : AdminMissingFoundTypeFilter.ALL).name();
        String statusValue = (statusFilter != null ? statusFilter : AdminMissingFoundStatusFilter.ALL).name();
        String categoryValue = category != null ? category.name() : null;
        return getSummaryInternal(normalizedSearch, typeValue, statusValue, categoryValue);
    }

    public MissingFoundReportDetails getReport(UUID reportId) {
        Alert alert = alertRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Alert", reportId));

        AdminMissingFoundReportRow row = alertJpaRepository.findMissingFoundReportForAdmin(reportId)
                .orElseThrow(() -> new NotFoundException("MissingFoundReport", reportId));

        return new MissingFoundReportDetails(
                alert.getId(),
                alert.getReporterId(),
                alert.getType().name(),
                alert.getCategory().name(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getLocation(),
                row.getReporterName(),
                row.getReporterUnitLabel(),
                alert.getStatus().name(),
                alert.getEventTime(),
                alert.getResolvedAt(),
                alert.getPhotoUrls(),
                alert.getContactNumber(),
                buildActionUrls(alert.getId())
        );
    }

    public List<String> getCategoryOptions() {
        Set<String> categories = new LinkedHashSet<>();
        categories.addAll(Arrays.stream(AlertCategory.values()).map(Enum::name).toList());
        categories.addAll(alertJpaRepository.findMissingFoundCategoriesForAdmin().stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .toList());
        return List.copyOf(categories);
    }

    @Transactional
    public void updateReport(UUID reportId, UpdateReportPayload payload) {
        Alert alert = alertRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Alert", reportId));

        AlertType type = payload.type() != null ? payload.type() : alert.getType();
        AlertCategory category = payload.category() != null ? payload.category() : alert.getCategory();
        String title = payload.title() != null ? payload.title() : alert.getTitle();
        String description = payload.description() != null ? payload.description() : alert.getDescription();
        String location = payload.location() != null ? payload.location() : alert.getLocation();
        Instant eventTime = payload.eventTime() != null ? payload.eventTime() : alert.getEventTime();
        List<String> photoUrls = payload.photoUrls() != null ? payload.photoUrls() : alert.getPhotoUrls();
        String contactNumber = payload.contactNumber() != null ? payload.contactNumber() : alert.getContactNumber();

        alert.updateDetails(type, category, title, description, location, eventTime, photoUrls, contactNumber);
        alertRepository.save(alert);
    }

    @Transactional
    public void updateStatus(UUID reportId, AlertReportStatus status) {
        Alert alert = alertRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Alert", reportId));

        alert.updateStatus(status);
        alertRepository.save(alert);
    }

    @Transactional
    public void deleteReport(UUID reportId) {
        if (!alertJpaRepository.existsById(reportId)) {
            throw new NotFoundException("Alert", reportId);
        }
        alertJpaRepository.deleteById(reportId);
    }

    private MissingFoundSummary getSummaryInternal(
            String search,
            String typeFilter,
            String statusFilter,
            String categoryFilter
    ) {
        AdminMissingFoundSummaryRow summary = alertJpaRepository.getMissingFoundSummaryForAdmin(
                search,
                typeFilter,
                statusFilter,
                categoryFilter
        );

        if (summary == null) {
            return new MissingFoundSummary(0, 0, 0, 0, 0, 0);
        }

        return new MissingFoundSummary(
                safeLong(summary.getTotalCount()),
                safeLong(summary.getMissingCount()),
                safeLong(summary.getFoundCount()),
                safeLong(summary.getOpenCount()),
                safeLong(summary.getMatchedCount()),
                safeLong(summary.getResolvedCount())
        );
    }

    private MissingFoundReportItem mapReportRow(AdminMissingFoundReportRow row) {
        return new MissingFoundReportItem(
                row.getReportId(),
                row.getReporterId(),
                row.getReportType(),
                row.getCategory(),
                row.getTitle(),
                row.getDescription(),
                row.getLocation(),
                row.getReporterName(),
                row.getReporterUnitLabel(),
                row.getStatus(),
                row.getContactNumber(),
                row.getEventTime(),
                row.getResolvedAt(),
                row.getCreatedAt(),
                buildActionUrls(row.getReportId())
        );
    }

    private MissingFoundActionUrls buildActionUrls(UUID reportId) {
        String baseUrl = "/v1/admin/missing-found/reports/" + reportId;
        return new MissingFoundActionUrls(
                baseUrl,
                baseUrl,
                baseUrl,
                baseUrl + "/status"
        );
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record MissingFoundActionUrls(
            String view,
            String edit,
            String delete,
            String updateStatus
    ) {
    }

    public record MissingFoundReportItem(
            UUID id,
            UUID reporterId,
            String reportType,
            String category,
            String title,
            String description,
            String location,
            String reporterName,
            String reporterUnitLabel,
            String status,
            String contactNumber,
            Instant eventTime,
            Instant resolvedAt,
            Instant createdAt,
            MissingFoundActionUrls actionUrls
    ) {
    }

    public record MissingFoundReportDetails(
            UUID id,
            UUID reporterId,
            String reportType,
            String category,
            String title,
            String description,
            String location,
            String reporterName,
            String reporterUnitLabel,
            String status,
            Instant eventTime,
            Instant resolvedAt,
            List<String> photoUrls,
            String contactNumber,
            MissingFoundActionUrls actionUrls
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

    public record MissingFoundReportsPage(
            List<MissingFoundReportItem> reports,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            long totalCount,
            long missingCount,
            long foundCount,
            long openCount,
            long matchedCount,
            long resolvedCount
    ) {
    }

    public record UpdateReportPayload(
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            String contactNumber
    ) {
    }
}