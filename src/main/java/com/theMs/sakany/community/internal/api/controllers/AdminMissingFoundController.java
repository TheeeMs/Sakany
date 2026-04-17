package com.theMs.sakany.community.internal.api.controllers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.theMs.sakany.accounts.internal.application.queries.AdminResidentDirectoryService;
import com.theMs.sakany.accounts.internal.application.queries.ResidentDirectoryStatus;
import com.theMs.sakany.accounts.internal.domain.ResidentApprovalStatus;
import com.theMs.sakany.community.internal.application.commands.ReportAlertCommand;
import com.theMs.sakany.community.internal.application.queries.AdminMissingFoundService;
import com.theMs.sakany.community.internal.application.queries.AdminMissingFoundStatusFilter;
import com.theMs.sakany.community.internal.application.queries.AdminMissingFoundTypeFilter;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/missing-found")
public class AdminMissingFoundController {

    private static final Pattern CONTACT_NUMBER_PATTERN = Pattern.compile("^[+]?[-0-9()\\s]{7,30}$");

    private final AdminMissingFoundService missingFoundService;
    private final CommandHandler<ReportAlertCommand, UUID> reportAlertHandler;
    private final AdminResidentDirectoryService residentDirectoryService;

    public AdminMissingFoundController(
            AdminMissingFoundService missingFoundService,
            CommandHandler<ReportAlertCommand, UUID> reportAlertHandler,
            AdminResidentDirectoryService residentDirectoryService
    ) {
        this.missingFoundService = missingFoundService;
        this.reportAlertHandler = reportAlertHandler;
        this.residentDirectoryService = residentDirectoryService;
    }

    @GetMapping("/reports")
    public ResponseEntity<AdminMissingFoundService.MissingFoundReportsPage> getReports(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(missingFoundService.getReports(
                search,
                AdminMissingFoundTypeFilter.from(type),
                AdminMissingFoundStatusFilter.from(status),
                parseCategoryFilter(category),
                page,
                size
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminMissingFoundService.MissingFoundSummary> getSummary(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(missingFoundService.getSummary(
                search,
                AdminMissingFoundTypeFilter.from(type),
                AdminMissingFoundStatusFilter.from(status),
                parseCategoryFilter(category)
        ));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<AdminMissingFoundService.MissingFoundReportDetails> getReport(@PathVariable UUID reportId) {
        return ResponseEntity.ok(missingFoundService.getReport(reportId));
    }

    @GetMapping("/reports/{reportId}/details")
    public ResponseEntity<AdminMissingFoundService.MissingFoundReportDetails> getReportDetails(@PathVariable UUID reportId) {
        return ResponseEntity.ok(missingFoundService.getReport(reportId));
    }

    @PostMapping("/reports")
    public ResponseEntity<UUID> createReport(@RequestBody AdminMissingFoundCreateRequest request) {
        UUID reportId = reportAlertHandler.handle(new ReportAlertCommand(
                request.resolveReporterId(),
                request.resolveType(),
                request.resolveCategory(),
                request.resolveTitle(),
                request.resolveDescription(),
                request.resolveLocation(),
                request.resolveEventTime(),
                request.resolvePhotoUrls(),
                request.resolveContactNumber()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(reportId);
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<Void> updateReport(
            @PathVariable UUID reportId,
            @RequestBody AdminMissingFoundUpdateRequest request
    ) {
        missingFoundService.updateReport(reportId, request.toPayload());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reports/{reportId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID reportId,
            @RequestBody AdminMissingFoundStatusRequest request
    ) {
        missingFoundService.updateStatus(reportId, request.resolveStatus());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reports/{reportId}/mark-matched")
    public ResponseEntity<Void> markMatched(@PathVariable UUID reportId) {
        missingFoundService.updateStatus(reportId, AlertReportStatus.MATCHED);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reports/{reportId}/mark-resolved")
    public ResponseEntity<Void> markResolved(@PathVariable UUID reportId) {
        missingFoundService.updateStatus(reportId, AlertReportStatus.RESOLVED);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports/{reportId}/notify-user")
    public ResponseEntity<AdminMissingFoundService.NotifyUserResult> notifyUser(
            @PathVariable UUID reportId,
            @RequestBody(required = false) AdminMissingFoundNotifyUserRequest request
    ) {
        AdminMissingFoundService.NotifyUserRequest payload = request == null
                ? new AdminMissingFoundService.NotifyUserRequest(null, null, NotificationChannel.IN_APP)
                : request.toPayload();

        return ResponseEntity.ok(missingFoundService.notifyReporter(reportId, payload));
    }

    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        missingFoundService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getTypeOptions() {
        return ResponseEntity.ok(Arrays.stream(AdminMissingFoundTypeFilter.values()).map(Enum::name).toList());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatusOptions() {
        return ResponseEntity.ok(Arrays.stream(AdminMissingFoundStatusFilter.values()).map(Enum::name).toList());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoryOptions() {
        return ResponseEntity.ok(missingFoundService.getCategoryOptions());
    }

    @GetMapping("/residents/options")
    public ResponseEntity<AdminResidentOptionsPage> getResidentOptions(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        AdminResidentDirectoryService.AdminResidentDirectoryPage residentsPage = residentDirectoryService.listResidents(
                search,
                null,
                ResidentDirectoryStatus.ACTIVE,
                ResidentApprovalStatus.APPROVED,
                page,
                size
        );

        List<AdminResidentOptionItem> options = residentsPage.residents().stream()
                .map(resident -> {
                    String unitLabel = buildUnitLabel(resident.buildingName(), resident.unitNumber());
                    return new AdminResidentOptionItem(
                            resident.residentId(),
                            resident.fullName(),
                            resident.phoneNumber(),
                            resident.unitNumber(),
                            resident.buildingName(),
                            unitLabel,
                            buildResidentDisplayLabel(resident.fullName(), unitLabel)
                    );
                })
                .toList();

        return ResponseEntity.ok(new AdminResidentOptionsPage(
                options,
                residentsPage.page(),
                residentsPage.size(),
                residentsPage.totalElements(),
                residentsPage.totalPages(),
                residentsPage.hasNext(),
                residentsPage.hasPrevious()
        ));
    }

    private static AlertCategory parseCategoryFilter(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            return null;
        }

        return parseReportCategory(rawCategory, true);
    }

    private static AlertType parseReportType(String rawType, boolean required) {
        if (rawType == null || rawType.isBlank()) {
            if (required) {
                throw new BusinessRuleException("reportType is required");
            }
            return null;
        }

        String normalized = normalizeEnumValue(rawType);
        if ("MISSING_ITEM".equals(normalized)) {
            normalized = "MISSING";
        }
        if ("FOUND_ITEM".equals(normalized)) {
            normalized = "FOUND";
        }

        AlertType parsed;
        try {
            parsed = AlertType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid reportType value: " + rawType);
        }

        if (parsed != AlertType.MISSING && parsed != AlertType.FOUND) {
            throw new BusinessRuleException("reportType must be MISSING or FOUND for admin Missing & Found");
        }

        return parsed;
    }

    private static AlertCategory parseReportCategory(String rawCategory, boolean required) {
        if (rawCategory == null || rawCategory.isBlank()) {
            if (required) {
                throw new BusinessRuleException("itemCategory is required");
            }
            return null;
        }

        String normalized = normalizeEnumValue(rawCategory);

        if (normalized.contains("PET")) {
            return AlertCategory.PET;
        }
        if (normalized.contains("VEHICLE") || normalized.contains("CAR") || normalized.contains("BIKE") || normalized.contains("SCOOTER")) {
            return AlertCategory.VEHICLE;
        }
        if (normalized.contains("PERSON") || normalized.contains("CHILD") || normalized.contains("ADULT")) {
            return AlertCategory.PERSON;
        }
        if (normalized.contains("ITEM")
                || normalized.contains("WALLET")
                || normalized.contains("KEY")
                || normalized.contains("PHONE")
                || normalized.contains("DOCUMENT")
                || normalized.contains("ELECTRONIC")
                || normalized.contains("JEWEL")
                || normalized.contains("BAG")
                || normalized.contains("CLOTH")) {
            return AlertCategory.ITEM;
        }
        if (normalized.contains("OTHER")) {
            return AlertCategory.OTHER;
        }

        try {
            return AlertCategory.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid itemCategory value: " + rawCategory);
        }
    }

    private static AlertReportStatus parseStatus(String rawStatus, boolean required) {
        if (rawStatus == null || rawStatus.isBlank()) {
            if (required) {
                throw new BusinessRuleException("status is required");
            }
            return null;
        }

        String normalized = normalizeEnumValue(rawStatus);
        try {
            return AlertReportStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid status value: " + rawStatus);
        }
    }

    private static NotificationChannel parseNotificationChannel(String rawChannel, NotificationChannel defaultChannel) {
        if (rawChannel == null || rawChannel.isBlank()) {
            return defaultChannel;
        }

        String normalized = normalizeEnumValue(rawChannel);
        try {
            return NotificationChannel.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid notify channel value: " + rawChannel);
        }
    }

    private static List<String> resolvePhotoUrls(List<String> primary, List<String> alias, boolean defaultEmpty) {
        List<String> selected = primary != null ? primary : alias;
        if (selected == null) {
            return defaultEmpty ? List.of() : null;
        }

        return selected.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private static String normalizeEnumValue(String rawValue) {
        return rawValue.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String requireNonBlank(String value, String fieldName) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BusinessRuleException(fieldName + " is required");
        }
        return normalized;
    }

    private static String validateContactNumberOrThrow(String value) {
        String normalized = requireNonBlank(value, "contactNumber");
        if (normalized.length() > 30) {
            throw new BusinessRuleException("contactNumber must be at most 30 characters");
        }
        if (!CONTACT_NUMBER_PATTERN.matcher(normalized).matches()) {
            throw new BusinessRuleException("contactNumber format is invalid");
        }
        return normalized;
    }

    private static String firstNonBlank(String primary, String fallback) {
        String primaryValue = normalizeOptional(primary);
        if (primaryValue != null) {
            return primaryValue;
        }
        return normalizeOptional(fallback);
    }

    private static String buildUnitLabel(String buildingName, String unitNumber) {
        String building = normalizeOptional(buildingName);
        String unit = normalizeOptional(unitNumber);

        if (building == null && unit == null) {
            return null;
        }
        if (building == null) {
            return unit;
        }
        if (unit == null) {
            return building;
        }
        return building + "-" + unit;
    }

    private static String buildResidentDisplayLabel(String fullName, String unitLabel) {
        String safeName = normalizeOptional(fullName);
        if (safeName == null) {
            safeName = "Unknown Resident";
        }

        if (unitLabel == null) {
            return safeName;
        }
        return safeName + " (" + unitLabel + ")";
    }

    public record AdminMissingFoundCreateRequest(
            UUID reporterId,
            @JsonAlias({"reportOnBehalfOf"}) UUID reportOnBehalfOf,
            UUID residentId,
            String type,
            @JsonAlias({"reportType", "report_type"}) String reportType,
            String category,
            @JsonAlias({"itemCategory", "categoryLabel"}) String itemCategory,
            @JsonAlias({"briefDescription"}) String title,
            String description,
            @JsonAlias({"detailedDescription"}) String detailedDescription,
            String location,
            @JsonAlias({"lastSeenLocation"}) String lastSeenLocation,
            Instant eventTime,
            @JsonAlias({"lastSeenAt"}) Instant lastSeenAt,
            List<String> photoUrls,
            @JsonAlias({"photos", "images", "attachments", "imageUrls"}) List<String> photos,
            @JsonAlias({"phoneNumber", "contact", "contact_phone"}) String contactNumber
    ) {
        UUID resolveReporterId() {
            if (reporterId != null) {
                return reporterId;
            }
            if (reportOnBehalfOf != null) {
                return reportOnBehalfOf;
            }
            if (residentId != null) {
                return residentId;
            }
            throw new BusinessRuleException("reporterId (or reportOnBehalfOf/residentId) is required");
        }

        AlertType resolveType() {
            return parseReportType(firstNonBlank(type, reportType), true);
        }

        AlertCategory resolveCategory() {
            return parseReportCategory(firstNonBlank(category, itemCategory), true);
        }

        String resolveTitle() {
            return requireNonBlank(title, "title");
        }

        String resolveDescription() {
            String value = firstNonBlank(description, detailedDescription);
            if (value == null) {
                throw new BusinessRuleException("description is required");
            }
            return value;
        }

        String resolveLocation() {
            return requireNonBlank(firstNonBlank(location, lastSeenLocation), "lastSeenLocation");
        }

        Instant resolveEventTime() {
            return eventTime != null ? eventTime : lastSeenAt;
        }

        List<String> resolvePhotoUrls() {
            return AdminMissingFoundController.resolvePhotoUrls(photoUrls, photos, true);
        }

        String resolveContactNumber() {
            return validateContactNumberOrThrow(contactNumber);
        }
    }

    public record AdminMissingFoundUpdateRequest(
            String type,
            @JsonAlias({"reportType", "report_type"}) String reportType,
            String category,
            @JsonAlias({"itemCategory", "categoryLabel"}) String itemCategory,
            @JsonAlias({"briefDescription"}) String title,
            String description,
            @JsonAlias({"detailedDescription"}) String detailedDescription,
            String location,
            @JsonAlias({"lastSeenLocation"}) String lastSeenLocation,
            Instant eventTime,
            @JsonAlias({"lastSeenAt"}) Instant lastSeenAt,
            List<String> photoUrls,
            @JsonAlias({"photos", "images", "attachments", "imageUrls"}) List<String> photos,
            @JsonAlias({"phoneNumber", "contact", "contact_phone"}) String contactNumber
    ) {
        AdminMissingFoundService.UpdateReportPayload toPayload() {
            AlertType resolvedType = parseReportType(firstNonBlank(type, reportType), false);
            AlertCategory resolvedCategory = parseReportCategory(firstNonBlank(category, itemCategory), false);

            return new AdminMissingFoundService.UpdateReportPayload(
                    resolvedType,
                    resolvedCategory,
                    normalizeOptional(title),
                    firstNonBlank(description, detailedDescription),
                    firstNonBlank(location, lastSeenLocation),
                    eventTime != null ? eventTime : lastSeenAt,
                    resolvePhotoUrls(photoUrls, photos, false),
                    normalizeOptional(contactNumber)
            );
        }
    }

    public record AdminMissingFoundStatusRequest(
            String status,
            @JsonAlias({"newStatus"}) String newStatus
    ) {
        AlertReportStatus resolveStatus() {
            return parseStatus(firstNonBlank(status, newStatus), true);
        }
    }

    public record AdminMissingFoundNotifyUserRequest(
            String title,
            @JsonAlias({"body", "notificationMessage"}) String message,
            @JsonAlias({"notifyChannel", "notificationChannel"}) String channel
    ) {
        AdminMissingFoundService.NotifyUserRequest toPayload() {
            return new AdminMissingFoundService.NotifyUserRequest(
                    normalizeOptional(title),
                    normalizeOptional(message),
                    parseNotificationChannel(channel, NotificationChannel.IN_APP)
            );
        }
    }

    public record AdminResidentOptionItem(
            UUID residentId,
            String fullName,
            String phoneNumber,
            String unitNumber,
            String buildingName,
            String unitLabel,
            String displayLabel
    ) {
    }

    public record AdminResidentOptionsPage(
            List<AdminResidentOptionItem> residents,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {
    }
}