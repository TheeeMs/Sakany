package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundReportRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AdminMissingFoundSummaryRow;
import com.theMs.sakany.community.internal.infrastructure.persistence.AlertJpaRepository;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommand;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommandHandler;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
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
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminMissingFoundService {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;

    private final AlertJpaRepository alertJpaRepository;
    private final AlertRepository alertRepository;
        private final SendNotificationCommandHandler sendNotificationCommandHandler;

    public AdminMissingFoundService(
            AlertJpaRepository alertJpaRepository,
                        AlertRepository alertRepository,
                        SendNotificationCommandHandler sendNotificationCommandHandler
    ) {
        this.alertJpaRepository = alertJpaRepository;
        this.alertRepository = alertRepository;
                this.sendNotificationCommandHandler = sendNotificationCommandHandler;
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
        Alert alert = getMissingFoundAlert(reportId);

        AdminMissingFoundReportRow row = alertJpaRepository.findMissingFoundReportForAdmin(reportId)
                .orElseThrow(() -> new NotFoundException("MissingFoundReport", reportId));

        Instant reportDate = row.getCreatedAt() != null ? row.getCreatedAt() : alert.getEventTime();
        String reportType = alert.getType().name();
        String category = alert.getCategory().name();
        String status = alert.getStatus().name();
        List<String> photoUrls = alert.getPhotoUrls();

        return new MissingFoundReportDetails(
                alert.getId(),
                alert.getReporterId(),
                reportType,
                toDisplayLabel(reportType),
                category,
                toDisplayLabel(category),
                alert.getTitle(),
                alert.getDescription(),
                alert.getDescription(),
                alert.getLocation(),
                alert.getLocation(),
                row.getReporterName(),
                row.getReporterName(),
                row.getReporterUnitLabel(),
                row.getReporterUnitLabel(),
                status,
                toDisplayLabel(status),
                alert.getEventTime(),
                reportDate,
                alert.getResolvedAt(),
                photoUrls,
                photoUrls,
                alert.getContactNumber(),
                getStatusOptions(),
                buildActionUrls(alert.getId())
        );
    }

    @Transactional
    public NotifyUserResult notifyReporter(UUID reportId, NotifyUserRequest request) {
                Alert alert = getMissingFoundAlert(reportId);

        String title = normalize(request.title());
        if (title == null) {
            title = "Update on your missing & found report";
        }

        String message = normalize(request.message());
        if (message == null) {
            message = buildDefaultNotificationMessage(alert);
        }

        NotificationChannel channel = request.channel() == null ? NotificationChannel.IN_APP : request.channel();

        UUID notificationId = sendNotificationCommandHandler.handle(new SendNotificationCommand(
                alert.getReporterId(),
                title,
                message,
                NotificationType.ALERT,
                reportId,
                channel
        ));

        return new NotifyUserResult(
                reportId,
                alert.getReporterId(),
                alert.getStatus().name(),
                title,
                message,
                channel.name(),
                notificationId
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
                Alert alert = getMissingFoundAlert(reportId);

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
                Alert alert = getMissingFoundAlert(reportId);

        alert.updateStatus(status);
        alertRepository.save(alert);
    }

    @Transactional
    public void deleteReport(UUID reportId) {
                getMissingFoundAlert(reportId);
        alertJpaRepository.deleteById(reportId);
    }

        private Alert getMissingFoundAlert(UUID reportId) {
                Alert alert = alertRepository.findById(reportId)
                                .orElseThrow(() -> new NotFoundException("Alert", reportId));

                if (alert.getType() != AlertType.MISSING && alert.getType() != AlertType.FOUND) {
                        throw new NotFoundException("MissingFoundReport", reportId);
                }

                return alert;
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
                                toDisplayLabel(row.getReportType()),
                row.getCategory(),
                                toDisplayLabel(row.getCategory()),
                row.getTitle(),
                row.getDescription(),
                                row.getDescription(),
                row.getLocation(),
                                row.getLocation(),
                row.getReporterName(),
                                row.getReporterName(),
                row.getReporterUnitLabel(),
                                row.getReporterUnitLabel(),
                row.getStatus(),
                                toDisplayLabel(row.getStatus()),
                row.getContactNumber(),
                row.getEventTime(),
                row.getResolvedAt(),
                row.getCreatedAt(),
                buildActionUrls(row.getReportId())
        );
    }

    private MissingFoundActionUrls buildActionUrls(UUID reportId) {
        String baseUrl = "/v1/admin/missing-found/reports/" + reportId;
                String updateStatusUrl = baseUrl + "/status";
        return new MissingFoundActionUrls(
                baseUrl,
                baseUrl,
                baseUrl,
                                updateStatusUrl,
                                baseUrl + "/mark-matched",
                                baseUrl + "/mark-resolved",
                                baseUrl + "/notify-user"
        );
    }

        private List<String> getStatusOptions() {
                return Arrays.stream(AlertReportStatus.values()).map(Enum::name).toList();
        }

        private String toDisplayLabel(String value) {
                if (value == null || value.isBlank()) {
                        return null;
                }

                String[] parts = value.trim().toLowerCase(Locale.ROOT).split("[_\\s]+");
                StringBuilder label = new StringBuilder();
                for (String part : parts) {
                        if (part.isBlank()) {
                                continue;
                        }
                        if (label.length() > 0) {
                                label.append(' ');
                        }
                        label.append(Character.toUpperCase(part.charAt(0)));
                        if (part.length() > 1) {
                                label.append(part.substring(1));
                        }
                }
                return label.toString();
        }

        private String buildDefaultNotificationMessage(Alert alert) {
                String statusLabel = toDisplayLabel(alert.getStatus().name());
                String typeLabel = toDisplayLabel(alert.getType().name());
                String location = normalize(alert.getLocation());
                if (location == null) {
                        location = "the reported location";
                }

                return "Your " + typeLabel.toLowerCase(Locale.ROOT)
                                + " report (\"" + alert.getTitle() + "\") is now " + statusLabel
                                + ". Last seen location: " + location + ".";
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
            String updateStatus,
            String markMatched,
            String markResolved,
            String notifyUser
    ) {
    }

    public record MissingFoundReportItem(
            UUID id,
            UUID reporterId,
            String reportType,
            String reportTypeLabel,
            String category,
            String categoryLabel,
            String title,
            String description,
            String detailedDescription,
            String location,
            String lastSeenLocation,
            String reporterName,
            String reportedByName,
            String reporterUnitLabel,
            String reportedByUnit,
            String status,
            String statusLabel,
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
            String reportTypeLabel,
            String category,
            String categoryLabel,
            String title,
            String description,
            String detailedDescription,
            String location,
            String lastSeenLocation,
            String reporterName,
            String reportedByName,
            String reporterUnitLabel,
            String reportedByUnit,
            String status,
            String statusLabel,
            Instant eventTime,
            Instant reportDate,
            Instant resolvedAt,
            List<String> photoUrls,
            List<String> photos,
            String contactNumber,
            List<String> statusOptions,
            MissingFoundActionUrls actionUrls
    ) {
    }

    public record NotifyUserRequest(
            String title,
            String message,
            NotificationChannel channel
    ) {
    }

    public record NotifyUserResult(
            UUID reportId,
            UUID reporterId,
            String reportStatus,
            String title,
            String message,
            String channel,
            UUID notificationId
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