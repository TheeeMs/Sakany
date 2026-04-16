package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.TechnicianProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.TechnicianProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.maintenance.internal.domain.MaintenancePriority;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceRequestEntity;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceRequestJpaRepository;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceTimelineEventEntity;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceTimelineEventJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitJpaRepository;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminMaintenanceCardService {

    private final MaintenanceRequestJpaRepository maintenanceRequestJpaRepository;
    private final MaintenanceTimelineEventJpaRepository maintenanceTimelineEventJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UnitJpaRepository unitJpaRepository;
    private final BuildingJpaRepository buildingJpaRepository;
    private final TechnicianProfileJpaRepository technicianProfileJpaRepository;

    public AdminMaintenanceCardService(
            MaintenanceRequestJpaRepository maintenanceRequestJpaRepository,
            MaintenanceTimelineEventJpaRepository maintenanceTimelineEventJpaRepository,
            UserJpaRepository userJpaRepository,
            UnitJpaRepository unitJpaRepository,
            BuildingJpaRepository buildingJpaRepository,
            TechnicianProfileJpaRepository technicianProfileJpaRepository
    ) {
        this.maintenanceRequestJpaRepository = maintenanceRequestJpaRepository;
        this.maintenanceTimelineEventJpaRepository = maintenanceTimelineEventJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.unitJpaRepository = unitJpaRepository;
        this.buildingJpaRepository = buildingJpaRepository;
        this.technicianProfileJpaRepository = technicianProfileJpaRepository;
    }

    public MaintenanceManagementCardResponse getCard(UUID requestId) {
        MaintenanceRequestEntity request = maintenanceRequestJpaRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("MaintenanceRequest", requestId));

        UserEntity resident = userJpaRepository.findById(request.getResidentId())
                .orElseThrow(() -> new NotFoundException("Resident", request.getResidentId()));

        UnitEntity unit = unitJpaRepository.findById(request.getUnitId()).orElse(null);
        BuildingEntity building = unit == null ? null : buildingJpaRepository.findById(unit.getBuildingId()).orElse(null);

        UserEntity technician = request.getTechnicianId() == null
                ? null
                : userJpaRepository.findById(request.getTechnicianId()).orElse(null);

        List<MaintenanceTimelineItem> timeline = maintenanceTimelineEventJpaRepository
                .findByRequestIdOrderByCreatedAtAsc(requestId)
                .stream()
                .map(this::mapTimeline)
                .toList();

        MaintenanceCompletion completion = new MaintenanceCompletion(
            "RESOLVED".equals(request.getStatus().name()),
            request.getResolvedAt(),
            request.getResolutionNotes(),
            request.getResolutionCost(),
            technician != null ? technician.getId() : null,
            technician != null ? buildFullName(technician.getFirstName(), technician.getLastName()) : null,
            "/v1/admin/maintenance/requests/" + request.getId() + "/receipt"
        );

        return new MaintenanceManagementCardResponse(
                request.getId(),
                request.getTitle(),
                request.isPublic() ? "PUBLIC" : "PRIVATE",
                mapPriorityToUi(request.getPriority()),
                mapStatusToUi(request.getStatus().name(), request.getTechnicianId()),
                request.getStatus().name(),
                request.getDescription(),
                resolveLocation(request, unit, building),
                request.getPhotoUrls() == null ? List.of() : request.getPhotoUrls(),
                new MaintenanceRequester(
                        resident.getId(),
                        buildFullName(resident.getFirstName(), resident.getLastName()),
                        buildInitials(resident.getFirstName(), resident.getLastName()),
                        resident.getPhone(),
                        resident.getEmail(),
                        unit != null ? unit.getUnitNumber() : null,
                        building != null ? building.getName() : null
                ),
                new MaintenanceAssignment(
                        request.getTechnicianId(),
                        technician != null ? buildFullName(technician.getFirstName(), technician.getLastName()) : null,
                        technician != null ? technician.getPhone() : null
                ),
                completion,
                timeline,
                request.getCreatedAt(),
                request.getResolvedAt()
        );
    }

    public List<TechnicianOption> getTechnicianOptions(boolean availableOnly) {
        List<TechnicianProfileEntity> technicians = availableOnly
                ? technicianProfileJpaRepository.findAvailableByUserRole(Role.TECHNICIAN)
                : technicianProfileJpaRepository.findAllByUserRole(Role.TECHNICIAN);

        return technicians.stream()
                .map(tp -> new TechnicianOption(
                        tp.getUser().getId(),
                        buildFullName(tp.getUser().getFirstName(), tp.getUser().getLastName()),
                        tp.getUser().getPhone(),
                        tp.isAvailable(),
                        tp.getSpecializations(),
                        tp.getRating()
                ))
                .toList();
    }

    private MaintenanceTimelineItem mapTimeline(MaintenanceTimelineEventEntity event) {
        return new MaintenanceTimelineItem(
                event.getEventType(),
                event.getTitle(),
                event.getDetails(),
                event.getActorId(),
                event.getCreatedAt()
        );
    }

    private String resolveLocation(MaintenanceRequestEntity request, UnitEntity unit, BuildingEntity building) {
        if (request.getLocationLabel() != null && !request.getLocationLabel().isBlank()) {
            return request.getLocationLabel();
        }
        if (unit != null && unit.getUnitNumber() != null) {
            return "Unit " + unit.getUnitNumber();
        }
        if (building != null) {
            return building.getName();
        }
        return "N/A";
    }

    private String mapPriorityToUi(MaintenancePriority priority) {
        if (priority == null) {
            return "MEDIUM";
        }
        return switch (priority) {
            case LOW -> "LOW";
            case NORMAL -> "MEDIUM";
            case URGENT, EMERGENCY -> "HIGH";
        };
    }

    private String mapStatusToUi(String workflowStatus, UUID technicianId) {
        if (workflowStatus == null) {
            return "UNASSIGNED";
        }
        return switch (workflowStatus) {
            case "RESOLVED" -> "COMPLETED";
            case "ASSIGNED", "IN_PROGRESS" -> "ASSIGNED";
            case "SUBMITTED" -> technicianId == null ? "UNASSIGNED" : "ASSIGNED";
            default -> "CLOSED";
        };
    }

    private String buildFullName(String firstName, String lastName) {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Unknown" : full;
    }

    private String buildInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            initials.append(Character.toUpperCase(firstName.trim().charAt(0)));
        }
        if (lastName != null && !lastName.isBlank()) {
            initials.append(Character.toUpperCase(lastName.trim().charAt(0)));
        }
        return initials.isEmpty() ? "NA" : initials.toString();
    }

    public record MaintenanceManagementCardResponse(
            UUID requestId,
            String issueTitle,
            String requestType,
            String priority,
            String status,
            String workflowStatus,
            String description,
            String location,
            List<String> photos,
            MaintenanceRequester requester,
            MaintenanceAssignment assignment,
                MaintenanceCompletion completion,
            List<MaintenanceTimelineItem> timeline,
            Instant submittedAt,
            Instant resolvedAt
    ) {
    }

    public record MaintenanceRequester(
            UUID residentId,
            String fullName,
            String initials,
            String phone,
            String email,
            String unitNumber,
            String buildingName
    ) {
    }

    public record MaintenanceAssignment(
            UUID technicianId,
            String technicianName,
            String technicianPhone
    ) {
    }

            public record MaintenanceCompletion(
                boolean completed,
                Instant completedAt,
                String resolution,
                BigDecimal totalCost,
                UUID completedById,
                String completedByName,
                String receiptDownloadUrl
            ) {
            }

    public record MaintenanceTimelineItem(
            String eventType,
            String title,
            String details,
            UUID actorId,
            Instant occurredAt
    ) {
    }

    public record TechnicianOption(
            UUID technicianId,
            String fullName,
            String phone,
            boolean isAvailable,
            List<String> specializations,
            Double rating
    ) {
    }
}
