package com.theMs.sakany.community.internal.api.controllers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.theMs.sakany.community.internal.application.commands.ReportAlertCommand;
import com.theMs.sakany.community.internal.application.queries.AdminMissingFoundService;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertType;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/missing-found")
public class AdminMissingFoundController {

    private final AdminMissingFoundService adminMissingFoundService;
    private final com.theMs.sakany.shared.cqrs.CommandHandler<ReportAlertCommand, UUID> reportAlertHandler;

    public AdminMissingFoundController(
            AdminMissingFoundService adminMissingFoundService,
            com.theMs.sakany.shared.cqrs.CommandHandler<ReportAlertCommand, UUID> reportAlertHandler
    ) {
        this.adminMissingFoundService = adminMissingFoundService;
        this.reportAlertHandler = reportAlertHandler;
    }

    @GetMapping("/reports")
    public ResponseEntity<AdminMissingFoundService.MissingFoundDashboardResponse> getDashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminMissingFoundService.getDashboard(search, type, status, category, page, size));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<AdminMissingFoundService.MissingFoundItem> getReport(@PathVariable UUID reportId) {
        return ResponseEntity.ok(adminMissingFoundService.getReport(reportId));
    }

    @GetMapping("/reports/{reportId}/history")
    public ResponseEntity<List<AdminMissingFoundService.MissingFoundHistoryItem>> getReportHistory(@PathVariable UUID reportId) {
        return ResponseEntity.ok(adminMissingFoundService.getReportHistory(reportId));
    }

    @PostMapping("/reports")
    public ResponseEntity<UUID> createReport(@RequestBody AdminCreateMissingFoundRequest request) {
        UUID reportId = reportAlertHandler.handle(new ReportAlertCommand(
                request.reporterId(),
                request.type(),
                request.category(),
                request.title(),
                request.description(),
                request.location(),
                request.eventTime(),
                request.photoUrls()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(reportId);
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<Void> updateReport(
            @PathVariable UUID reportId,
            @RequestBody AdminUpdateMissingFoundRequest request
    ) {
        adminMissingFoundService.updateReport(
                reportId,
                request.title(),
                request.description(),
                request.location(),
                request.category(),
                request.eventTime(),
                request.photoUrls()
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reports/{reportId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID reportId,
            @RequestBody AdminUpdateMissingFoundStatusRequest request
    ) {
        adminMissingFoundService.updateReportStatus(reportId, request.status());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID reportId) {
        adminMissingFoundService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getTypeOptions() {
        return ResponseEntity.ok(adminMissingFoundService.getTypeOptions());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatusOptions() {
        return ResponseEntity.ok(adminMissingFoundService.getStatusOptions());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoryOptions() {
        return ResponseEntity.ok(adminMissingFoundService.getCategoryOptions());
    }

    public record AdminCreateMissingFoundRequest(
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            @JsonAlias({"images", "photoUrls", "attachments"})
            List<String> photoUrls
    ) {
    }

    public record AdminUpdateMissingFoundRequest(
            String title,
            String description,
            String location,
            AlertCategory category,
            Instant eventTime,
            @JsonAlias({"images", "photoUrls", "attachments"})
            List<String> photoUrls
    ) {
    }

    public record AdminUpdateMissingFoundStatusRequest(AlertReportStatus status) {
    }
}
