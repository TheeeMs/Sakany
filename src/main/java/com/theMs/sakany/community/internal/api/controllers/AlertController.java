package com.theMs.sakany.community.internal.api.controllers;

import com.theMs.sakany.community.internal.application.commands.ReportAlertCommand;
import com.theMs.sakany.community.internal.application.commands.ResolveAlertCommand;
import com.theMs.sakany.community.internal.application.queries.GetActiveAlertsQuery;
import com.theMs.sakany.community.internal.application.queries.GetAlertByIdQuery;
import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.community.internal.domain.AlertReportStatus;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/alerts")
public class AlertController {

    private final CommandHandler<ReportAlertCommand, UUID> reportAlertHandler;
    private final CommandHandler<ResolveAlertCommand, Void> resolveAlertHandler;
    private final QueryHandler<GetActiveAlertsQuery, List<Alert>> getActiveAlertsHandler;
    private final QueryHandler<GetAlertByIdQuery, Optional<Alert>> getAlertByIdHandler;

    public AlertController(
            CommandHandler<ReportAlertCommand, UUID> reportAlertHandler,
            CommandHandler<ResolveAlertCommand, Void> resolveAlertHandler,
            QueryHandler<GetActiveAlertsQuery, List<Alert>> getActiveAlertsHandler,
            QueryHandler<GetAlertByIdQuery, Optional<Alert>> getAlertByIdHandler
    ) {
        this.reportAlertHandler = reportAlertHandler;
        this.resolveAlertHandler = resolveAlertHandler;
        this.getActiveAlertsHandler = getActiveAlertsHandler;
        this.getAlertByIdHandler = getAlertByIdHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> reportAlert(@RequestBody ReportAlertRequest request) {
        UUID alertId = reportAlertHandler.handle(new ReportAlertCommand(
                request.reporterId(),
                request.type(),
                request.category(),
                request.title(),
                request.description(),
                request.location(),
                request.eventTime(),
                request.photoUrls(),
                request.contactNumber()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(alertId);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        List<Alert> alerts = getActiveAlertsHandler.handle(new GetActiveAlertsQuery());
        List<AlertResponse> response = alerts.stream().map(AlertResponse::from).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable UUID id) {
        Optional<Alert> alertOpt = getAlertByIdHandler.handle(new GetAlertByIdQuery(id));
        return alertOpt
                .map(AlertResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable UUID id, @RequestBody ResolveAlertRequest request) {
        resolveAlertHandler.handle(new ResolveAlertCommand(id, request.requestingUserId()));
        return ResponseEntity.noContent().build();
    }

    public record ReportAlertRequest(
            UUID reporterId,
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

    public record ResolveAlertRequest(UUID requestingUserId) {
    }

    public record AlertResponse(
            UUID id,
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            String contactNumber,
            AlertReportStatus status,
            boolean isResolved,
            Instant resolvedAt
    ) {
        public static AlertResponse from(Alert alert) {
            return new AlertResponse(
                    alert.getId(),
                    alert.getReporterId(),
                    alert.getType(),
                    alert.getCategory(),
                    alert.getTitle(),
                    alert.getDescription(),
                    alert.getLocation(),
                    alert.getEventTime(),
                    alert.getPhotoUrls(),
                    alert.getContactNumber(),
                    alert.getStatus(),
                    alert.isResolved(),
                    alert.getResolvedAt()
            );
        }
    }
}
