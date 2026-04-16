package com.theMs.sakany.community.internal.api.controllers;

import com.theMs.sakany.community.internal.application.commands.ReportAlertCommand;
import com.theMs.sakany.community.internal.application.commands.ResolveAlertCommand;
import com.theMs.sakany.community.internal.application.queries.GetActiveAlertsQuery;
import com.theMs.sakany.community.internal.application.queries.GetAlertByIdQuery;
import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/alerts")
public class AlertController {

    private final com.theMs.sakany.shared.cqrs.CommandHandler<ReportAlertCommand, UUID> reportAlertHandler;
    private final com.theMs.sakany.shared.cqrs.CommandHandler<ResolveAlertCommand, Void> resolveAlertHandler;
    private final com.theMs.sakany.shared.cqrs.QueryHandler<GetActiveAlertsQuery, List<Alert>> getActiveAlertsHandler;
    private final com.theMs.sakany.shared.cqrs.QueryHandler<GetAlertByIdQuery, Optional<Alert>> getAlertByIdHandler;

    public AlertController(
        com.theMs.sakany.shared.cqrs.CommandHandler<ReportAlertCommand, UUID> reportAlertHandler,
        com.theMs.sakany.shared.cqrs.CommandHandler<ResolveAlertCommand, Void> resolveAlertHandler,
        com.theMs.sakany.shared.cqrs.QueryHandler<GetActiveAlertsQuery, List<Alert>> getActiveAlertsHandler,
        com.theMs.sakany.shared.cqrs.QueryHandler<GetAlertByIdQuery, Optional<Alert>> getAlertByIdHandler
    ) {
        this.reportAlertHandler = reportAlertHandler;
        this.resolveAlertHandler = resolveAlertHandler;
        this.getActiveAlertsHandler = getActiveAlertsHandler;
        this.getAlertByIdHandler = getAlertByIdHandler;
    }

    public record ReportAlertRequest(
        UUID reporterId,
        AlertType type,
        AlertCategory category,
        String title,
        String description,
        String location,
        Instant eventTime,
        List<String> photoUrls
    ) {}

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
                alert.isResolved(),
                alert.getResolvedAt()
            );
        }
    }

    public record ResolveAlertRequest(UUID requestingUserId) {}

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
            request.photoUrls()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(alertId);
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getActiveAlerts() {
        List<Alert> alerts = getActiveAlertsHandler.handle(new GetActiveAlertsQuery());
        List<AlertResponse> response = alerts.stream().map(AlertResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable UUID id) {
        Optional<Alert> alertOpt = getAlertByIdHandler.handle(new GetAlertByIdQuery(id));
        return alertOpt.map(alert -> ResponseEntity.ok(AlertResponse.from(alert)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolveAlert(@PathVariable UUID id, @RequestBody ResolveAlertRequest request) {
        resolveAlertHandler.handle(new ResolveAlertCommand(id, request.requestingUserId()));
        return ResponseEntity.noContent().build();
    }
}
