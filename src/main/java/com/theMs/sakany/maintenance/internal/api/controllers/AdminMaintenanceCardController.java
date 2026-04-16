package com.theMs.sakany.maintenance.internal.api.controllers;

import com.theMs.sakany.maintenance.internal.application.commands.AssignTechnicianCommand;
import com.theMs.sakany.maintenance.internal.application.commands.AssignTechnicianCommandHandler;
import com.theMs.sakany.maintenance.internal.application.commands.UpdateMaintenancePriorityCommand;
import com.theMs.sakany.maintenance.internal.application.commands.UpdateMaintenancePriorityCommandHandler;
import com.theMs.sakany.maintenance.internal.application.queries.AdminMaintenanceCardService;
import com.theMs.sakany.maintenance.internal.application.queries.MaintenanceTimelineEventType;
import com.theMs.sakany.maintenance.internal.application.queries.MaintenanceTimelineService;
import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/maintenance")
public class AdminMaintenanceCardController {

    private final AdminMaintenanceCardService adminMaintenanceCardService;
    private final AssignTechnicianCommandHandler assignTechnicianCommandHandler;
    private final UpdateMaintenancePriorityCommandHandler updateMaintenancePriorityCommandHandler;
    private final MaintenanceTimelineService maintenanceTimelineService;

    public AdminMaintenanceCardController(
            AdminMaintenanceCardService adminMaintenanceCardService,
            AssignTechnicianCommandHandler assignTechnicianCommandHandler,
            UpdateMaintenancePriorityCommandHandler updateMaintenancePriorityCommandHandler,
            MaintenanceTimelineService maintenanceTimelineService
    ) {
        this.adminMaintenanceCardService = adminMaintenanceCardService;
        this.assignTechnicianCommandHandler = assignTechnicianCommandHandler;
        this.updateMaintenancePriorityCommandHandler = updateMaintenancePriorityCommandHandler;
        this.maintenanceTimelineService = maintenanceTimelineService;
    }

    @GetMapping("/requests/{requestId}/card")
    public ResponseEntity<AdminMaintenanceCardService.MaintenanceManagementCardResponse> getMaintenanceCard(
            @PathVariable UUID requestId
    ) {
        return ResponseEntity.ok(adminMaintenanceCardService.getCard(requestId));
    }

    @GetMapping("/requests/{requestId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable UUID requestId) {
        AdminMaintenanceCardService.MaintenanceManagementCardResponse card = adminMaintenanceCardService.getCard(requestId);

        StringBuilder content = new StringBuilder();
        content.append("Sakany Maintenance Receipt\n");
        content.append("Request ID: ").append(card.requestId()).append("\n");
        content.append("Issue: ").append(card.issueTitle()).append("\n");
        content.append("Resident: ").append(card.requester().fullName()).append("\n");
        content.append("Location: ").append(card.location()).append("\n");
        content.append("Status: ").append(card.status()).append("\n");

        if (card.completion() != null) {
            if (card.completion().completedAt() != null) {
                content.append("Completed At: ").append(card.completion().completedAt()).append("\n");
            }
            if (card.completion().completedByName() != null) {
                content.append("Completed By: ").append(card.completion().completedByName()).append("\n");
            }
            if (card.completion().resolution() != null) {
                content.append("Resolution: ").append(card.completion().resolution()).append("\n");
            }
            if (card.completion().totalCost() != null) {
                content.append("Total Cost: ").append(card.completion().totalCost()).append(" EGP\n");
            }
        }

        byte[] bytes = content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=maintenance-receipt-" + requestId + ".txt")
                .body(bytes);
    }

    @GetMapping("/technicians")
    public ResponseEntity<List<AdminMaintenanceCardService.TechnicianOption>> getTechnicians(
            @RequestParam(defaultValue = "true") boolean availableOnly
    ) {
        return ResponseEntity.ok(adminMaintenanceCardService.getTechnicianOptions(availableOnly));
    }

    @PatchMapping("/requests/{requestId}/priority")
    public ResponseEntity<Void> updatePriority(
            @PathVariable UUID requestId,
            @RequestBody UpdatePriorityRequest request
    ) {
        MaintenancePriority mappedPriority = mapUiPriority(request.priority());
        updateMaintenancePriorityCommandHandler.handle(new UpdateMaintenancePriorityCommand(requestId, mappedPriority));

        maintenanceTimelineService.record(
                requestId,
                MaintenanceTimelineEventType.PRIORITY_UPDATED,
                "Priority Updated",
                "Priority set to " + request.priority(),
                request.actorId()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{requestId}/assign")
    public ResponseEntity<Void> assignTechnician(
            @PathVariable UUID requestId,
            @RequestBody AssignMaintenanceTechnicianRequest request
    ) {
        if (request.technicianId() == null) {
            throw new BusinessRuleException("technicianId is required");
        }

        assignTechnicianCommandHandler.handle(new AssignTechnicianCommand(requestId, request.technicianId()));

        maintenanceTimelineService.record(
                requestId,
                MaintenanceTimelineEventType.TECHNICIAN_ASSIGNED,
                "Technician Assigned",
                request.assignmentNote(),
                request.actorId()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{requestId}/viewed")
    public ResponseEntity<Void> markViewed(
            @PathVariable UUID requestId,
            @RequestBody(required = false) MarkMaintenanceViewedRequest request
    ) {
        UUID actorId = request != null ? request.actorId() : null;
        maintenanceTimelineService.record(
                requestId,
                MaintenanceTimelineEventType.ADMIN_VIEWED,
                "Admin Viewed",
                null,
                actorId
        );
        return ResponseEntity.noContent().build();
    }

    private MaintenancePriority mapUiPriority(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("priority is required");
        }

        return switch (value.trim().toUpperCase()) {
            case "HIGH" -> MaintenancePriority.URGENT;
            case "MEDIUM" -> MaintenancePriority.NORMAL;
            case "LOW" -> MaintenancePriority.LOW;
            default -> throw new BusinessRuleException("priority must be one of HIGH, MEDIUM, LOW");
        };
    }

    public record UpdatePriorityRequest(String priority, UUID actorId) {
    }

    public record AssignMaintenanceTechnicianRequest(UUID technicianId, UUID actorId, String assignmentNote) {
    }

    public record MarkMaintenanceViewedRequest(UUID actorId) {
    }
}
