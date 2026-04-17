package com.theMs.sakany.maintenance.internal.api.controllers;

import com.theMs.sakany.maintenance.internal.api.dto.AssignTechnicianDto;
import com.theMs.sakany.maintenance.internal.api.dto.CreateMaintenanceRequestDto;
import com.theMs.sakany.maintenance.internal.api.dto.MaintenanceRequestResponseDto;
import com.theMs.sakany.maintenance.internal.api.dto.RejectRequestDto;
import com.theMs.sakany.maintenance.internal.api.dto.ResolveMaintenanceRequestDto;
import com.theMs.sakany.maintenance.internal.application.commands.*;
import com.theMs.sakany.maintenance.internal.application.queries.*;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceStatus;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/maintenance-requests")
public class MaintenanceRequestController {

    private final CreateMaintenanceRequestCommandHandler createHandler;
    private final AssignTechnicianCommandHandler assignHandler;
    private final StartWorkCommandHandler startWorkHandler;
    private final ResolveCommandHandler resolveHandler;
    private final CancelCommandHandler cancelHandler;
    private final RejectCommandHandler rejectHandler;
    private final MaintenanceTimelineService timelineService;

    private final GetMaintenanceRequestByIdQueryHandler getByIdHandler;
    private final GetMaintenanceRequestsByResidentQueryHandler getByResidentHandler;
    private final GetMaintenanceRequestsByStatusQueryHandler getByStatusHandler;

    public MaintenanceRequestController(
            CreateMaintenanceRequestCommandHandler createHandler,
            AssignTechnicianCommandHandler assignHandler,
            StartWorkCommandHandler startWorkHandler,
            ResolveCommandHandler resolveHandler,
            CancelCommandHandler cancelHandler,
            RejectCommandHandler rejectHandler,
            GetMaintenanceRequestByIdQueryHandler getByIdHandler,
            GetMaintenanceRequestsByResidentQueryHandler getByResidentHandler,
            GetMaintenanceRequestsByStatusQueryHandler getByStatusHandler,
            MaintenanceTimelineService timelineService) {
        this.createHandler = createHandler;
        this.assignHandler = assignHandler;
        this.startWorkHandler = startWorkHandler;
        this.resolveHandler = resolveHandler;
        this.cancelHandler = cancelHandler;
        this.rejectHandler = rejectHandler;
        this.getByIdHandler = getByIdHandler;
        this.getByResidentHandler = getByResidentHandler;
        this.getByStatusHandler = getByStatusHandler;
        this.timelineService = timelineService;
    }

    @PostMapping
    public ResponseEntity<UUID> createRequest(@RequestBody CreateMaintenanceRequestDto dto) {
        UUID actorId = getAuthenticatedUserId();
        if (!isAdminActor() && (dto.residentId() == null || !actorId.equals(dto.residentId()))) {
            throw new BusinessRuleException("residentId must match authenticated user");
        }

        CreateMaintenanceRequestCommand command = new CreateMaintenanceRequestCommand(
                dto.residentId(),
                dto.unitId(),
                dto.title(),
                dto.description(),
                dto.locationLabel(),
                dto.category(),
                dto.priority(),
                dto.isPublic(),
                dto.photoUrls()
        );
        UUID id = createHandler.handle(command);

        timelineService.record(
            id,
            MaintenanceTimelineEventType.REQUEST_SUBMITTED,
            "Request Submitted",
            null,
            dto.residentId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponseDto> getById(@PathVariable UUID id) {
        MaintenanceRequest request = getByIdHandler.handle(new GetMaintenanceRequestByIdQuery(id));
        return ResponseEntity.ok(MaintenanceRequestResponseDto.fromDomain(request));
    }

    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<MaintenanceRequestResponseDto>> getByResident(@PathVariable UUID residentId) {
        List<MaintenanceRequest> requests = getByResidentHandler.handle(new GetMaintenanceRequestsByResidentQuery(residentId));
        return ResponseEntity.ok(requests.stream()
                .map(MaintenanceRequestResponseDto::fromDomain)
                .collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaintenanceRequestResponseDto>> getByStatus(@PathVariable MaintenanceStatus status) {
        List<MaintenanceRequest> requests = getByStatusHandler.handle(new GetMaintenanceRequestsByStatusQuery(status));
        return ResponseEntity.ok(requests.stream()
                .map(MaintenanceRequestResponseDto::fromDomain)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<Void> assignTechnician(@PathVariable UUID id, @RequestBody AssignTechnicianDto dto) {
        authorizeMutationForRequest(id);
        assignHandler.handle(new AssignTechnicianCommand(id, dto.technicianId()));

        timelineService.record(
                id,
                MaintenanceTimelineEventType.TECHNICIAN_ASSIGNED,
                "Technician Assigned",
                null,
                dto.technicianId()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> startWork(@PathVariable UUID id) {
        authorizeMutationForRequest(id);
        startWorkHandler.handle(new StartWorkCommand(id));

        timelineService.record(
                id,
                MaintenanceTimelineEventType.WORK_STARTED,
                "Work Started",
                null,
                null
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resolve")
        public ResponseEntity<Void> resolve(
            @PathVariable UUID id,
            @RequestBody(required = false) ResolveMaintenanceRequestDto dto
        ) {
            authorizeMutationForRequest(id);
        String resolution = dto != null ? dto.resolution() : null;
        java.math.BigDecimal totalCost = dto != null ? dto.totalCost() : null;

        resolveHandler.handle(new ResolveCommand(id, resolution, totalCost));

        timelineService.record(
                id,
                MaintenanceTimelineEventType.REQUEST_RESOLVED,
                "Request Resolved",
            buildResolutionTimelineDetails(resolution, totalCost),
                null
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        authorizeMutationForRequest(id);
        cancelHandler.handle(new CancelCommand(id));

        timelineService.record(
                id,
                MaintenanceTimelineEventType.REQUEST_CANCELLED,
                "Request Cancelled",
                null,
                null
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID id, @RequestBody RejectRequestDto dto) {
        authorizeMutationForRequest(id);
        rejectHandler.handle(new RejectCommand(id, dto.reason()));

        timelineService.record(
                id,
                MaintenanceTimelineEventType.REQUEST_REJECTED,
                "Request Rejected",
                dto.reason(),
                null
        );
        return ResponseEntity.ok().build();
    }

    private String buildResolutionTimelineDetails(String resolution, java.math.BigDecimal totalCost) {
        if ((resolution == null || resolution.isBlank()) && totalCost == null) {
            return null;
        }

        StringBuilder details = new StringBuilder();
        if (resolution != null && !resolution.isBlank()) {
            details.append("Resolution: ").append(resolution.trim());
        }
        if (totalCost != null) {
            if (!details.isEmpty()) {
                details.append(" | ");
            }
            details.append("Cost: ").append(totalCost).append(" EGP");
        }
        return details.toString();
    }

    private void authorizeMutationForRequest(UUID requestId) {
        UUID actorId = getAuthenticatedUserId();
        if (isAdminActor()) {
            return;
        }

        MaintenanceRequest request = getByIdHandler.handle(new GetMaintenanceRequestByIdQuery(requestId));
        if (!actorId.equals(request.getResidentId())) {
            throw new BusinessRuleException("You are not allowed to modify this maintenance request");
        }
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }

        try {
            return UUID.fromString(principal.toString());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid authenticated principal");
        }
    }

    private boolean isAdminActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }
}
