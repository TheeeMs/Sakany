package com.theMs.sakany.events.internal.api.controllers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.theMs.sakany.events.internal.application.commands.ApproveEventCommand;
import com.theMs.sakany.events.internal.application.commands.ApproveEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.CancelEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.CompleteEventCommand;
import com.theMs.sakany.events.internal.application.commands.CompleteEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.ProposeEventCommand;
import com.theMs.sakany.events.internal.application.commands.ProposeEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.RejectEventCommand;
import com.theMs.sakany.events.internal.application.commands.RejectEventCommandHandler;
import com.theMs.sakany.events.internal.application.queries.AdminEventCardMenuService;
import com.theMs.sakany.events.internal.application.queries.AdminEventStatusFilter;
import com.theMs.sakany.events.internal.application.queries.AdminEventsManagerService;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/v1/admin/events")
public class AdminEventsController {

    private final AdminEventsManagerService adminEventsManagerService;
    private final AdminEventCardMenuService adminEventCardMenuService;
    private final ProposeEventCommandHandler proposeEventCommandHandler;
    private final ApproveEventCommandHandler approveEventCommandHandler;
    private final RejectEventCommandHandler rejectEventCommandHandler;
    private final CompleteEventCommandHandler completeEventCommandHandler;
    private final CancelEventCommandHandler cancelEventCommandHandler;

    public AdminEventsController(
            AdminEventsManagerService adminEventsManagerService,
            AdminEventCardMenuService adminEventCardMenuService,
            ProposeEventCommandHandler proposeEventCommandHandler,
            ApproveEventCommandHandler approveEventCommandHandler,
            RejectEventCommandHandler rejectEventCommandHandler,
            CompleteEventCommandHandler completeEventCommandHandler,
            CancelEventCommandHandler cancelEventCommandHandler
    ) {
        this.adminEventsManagerService = adminEventsManagerService;
        this.adminEventCardMenuService = adminEventCardMenuService;
        this.proposeEventCommandHandler = proposeEventCommandHandler;
        this.approveEventCommandHandler = approveEventCommandHandler;
        this.rejectEventCommandHandler = rejectEventCommandHandler;
        this.completeEventCommandHandler = completeEventCommandHandler;
        this.cancelEventCommandHandler = cancelEventCommandHandler;
    }

    @GetMapping
    public ResponseEntity<AdminEventsManagerService.AdminEventsDashboardResponse> getDashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") AdminEventStatusFilter status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        return ResponseEntity.ok(adminEventsManagerService.getDashboard(search, status, category, page, size));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoryOptions() {
        return ResponseEntity.ok(adminEventsManagerService.getCategoryOptions());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatusOptions() {
        return ResponseEntity.ok(adminEventsManagerService.getStatusOptions());
    }

    @PostMapping
    public ResponseEntity<UUID> createEvent(@RequestBody AdminCreateEventRequest request) {
        UUID organizerId = request.organizerId() != null ? request.organizerId() : getAuthenticatedUserId();
        Instant startDate = request.resolveStartDate();
        Instant endDate = request.resolveEndDate(startDate);

        UUID eventId = proposeEventCommandHandler.handle(new ProposeEventCommand(
                organizerId,
                request.title(),
                request.description(),
                request.location(),
            startDate,
            endDate,
                request.imageUrl(),
                request.hostName(),
                request.price(),
                request.maxAttendees(),
                request.category(),
                request.hostRole(),
            request.resolveContactPhone(),
                request.latitude(),
            request.longitude(),
            request.resolveTags(),
            request.resolveRecurringEvent()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(eventId);
    }

    @PatchMapping("/{eventId}/approve")
    public ResponseEntity<Void> approveEvent(@PathVariable UUID eventId, @RequestBody(required = false) AdminActorRequest request) {
        UUID requestedAdminId = request != null ? request.adminId() : null;
        UUID adminId = resolveAdminActorId(requestedAdminId);
        approveEventCommandHandler.handle(new ApproveEventCommand(eventId, adminId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/reject")
    public ResponseEntity<Void> rejectEvent(@PathVariable UUID eventId, @RequestBody(required = false) AdminActorRequest request) {
        UUID requestedAdminId = request != null ? request.adminId() : null;
        UUID adminId = resolveAdminActorId(requestedAdminId);
        rejectEventCommandHandler.handle(new RejectEventCommand(eventId, adminId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/complete")
    public ResponseEntity<Void> completeEvent(@PathVariable UUID eventId, @RequestBody(required = false) AdminActorRequest request) {
        UUID requestedAdminId = request != null ? request.adminId() : null;
        UUID adminId = resolveAdminActorId(requestedAdminId);
        completeEventCommandHandler.handle(new CompleteEventCommand(eventId, adminId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/details")
    public ResponseEntity<AdminEventCardMenuService.AdminEventCardDetailsResponse> getEventCardDetails(@PathVariable UUID eventId) {
        return ResponseEntity.ok(adminEventCardMenuService.getDetails(eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<Void> editEvent(
            @PathVariable UUID eventId,
            @RequestBody AdminEventCardMenuService.AdminUpdateEventRequest request
    ) {
        adminEventCardMenuService.editEvent(eventId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/notify-residents")
    public ResponseEntity<AdminEventCardMenuService.NotifyResidentsResult> notifyResidents(
            @PathVariable UUID eventId,
            @RequestBody(required = false) AdminNotifyResidentsRequest request
    ) {
        AdminEventCardMenuService.NotifyResidentsRequest notifyRequest = new AdminEventCardMenuService.NotifyResidentsRequest(
                request != null ? request.title() : null,
                request != null ? request.message() : null,
                request != null ? request.channel() : null
        );
        return ResponseEntity.ok(adminEventCardMenuService.notifyResidents(eventId, notifyRequest));
    }

    @GetMapping("/{eventId}/attendees/export")
    public ResponseEntity<byte[]> exportAttendees(@PathVariable UUID eventId) {
        byte[] body = adminEventCardMenuService.exportAttendees(eventId);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=event-attendees-" + eventId + ".csv")
                .body(body);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID adminId
    ) {
        UUID effectiveAdminId = resolveAdminActorId(adminId);
        adminEventCardMenuService.deleteEvent(eventId, effectiveAdminId);
        return ResponseEntity.noContent().build();
    }

    private UUID resolveAdminActorId(UUID requestedAdminId) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        if (requestedAdminId != null && !requestedAdminId.equals(authenticatedUserId)) {
            throw new BusinessRuleException("adminId must match authenticated user");
        }
        return authenticatedUserId;
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

    public record AdminCreateEventRequest(
            UUID organizerId,
            @JsonAlias({"eventTitle"})
            String title,
            @JsonAlias({"eventDescription"})
            String description,
            @JsonAlias({"eventLocation"})
            String location,
            Instant startDate,
            Instant endDate,
            @JsonAlias({"eventCoverImage", "eventCoverImageUrl", "coverImageUrl"})
            String imageUrl,
            String hostName,
            Double price,
            @JsonAlias({"maximumCapacity"})
            Integer maxAttendees,
            @JsonAlias({"eventCategory"})
            String category,
            String hostRole,
            @JsonAlias({"contactNumber"})
            String contactPhone,
            Double latitude,
            Double longitude,
            String date,
            String time,
            Integer duration,
            String durationUnit,
            JsonNode tags,
            @JsonAlias({"isRecurring"})
            Boolean recurringEvent
    ) {
        private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("M/d/uuuu"),
                DateTimeFormatter.ofPattern("MM/dd/uuuu"),
                DateTimeFormatter.ofPattern("d/M/uuuu"),
                DateTimeFormatter.ofPattern("dd/MM/uuuu")
        );

        private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)
        );

        public Instant resolveStartDate() {
            if (startDate != null) {
                return startDate;
            }

            if (isBlank(date)) {
                return null;
            }

            LocalDate parsedDate = parseDate(date);
            if (isBlank(time)) {
                throw new BusinessRuleException("time is required when startDate is not provided");
            }

            LocalTime parsedTime = parseTime(time);
            LocalDateTime dateTime = LocalDateTime.of(parsedDate, parsedTime);
            return dateTime.toInstant(ZoneOffset.UTC);
        }

        public Instant resolveEndDate(Instant effectiveStartDate) {
            if (endDate != null) {
                return endDate;
            }

            if (effectiveStartDate == null || duration == null) {
                return null;
            }

            if (duration <= 0) {
                throw new BusinessRuleException("duration must be greater than 0");
            }

            String normalizedUnit = isBlank(durationUnit) ? "HOUR" : durationUnit.trim().toUpperCase(Locale.ROOT);
            Duration effectiveDuration = switch (normalizedUnit) {
                case "HOUR", "HOURS" -> Duration.ofHours(duration.longValue());
                case "DAY", "DAYS" -> Duration.ofDays(duration.longValue());
                default -> throw new BusinessRuleException("durationUnit must be HOUR or DAY");
            };

            return effectiveStartDate.plus(effectiveDuration);
        }

        public String resolveContactPhone() {
            if (isBlank(contactPhone)) {
                return null;
            }

            String trimmed = contactPhone.trim();
            if (trimmed.startsWith("00") && trimmed.length() > 2) {
                return "+" + trimmed.substring(2);
            }

            return trimmed;
        }

        public String resolveTags() {
            if (tags == null || tags.isNull()) {
                return null;
            }

            if (tags.isTextual()) {
                return normalizeTags(tags.asText());
            }

            if (tags.isArray()) {
                String joined = StreamSupport.stream(tags.spliterator(), false)
                        .map(JsonNode::asText)
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .reduce((left, right) -> left + "," + right)
                        .orElse("");
                return normalizeTags(joined);
            }

            throw new BusinessRuleException("tags must be a comma-separated string or an array");
        }

        public boolean resolveRecurringEvent() {
            return Boolean.TRUE.equals(recurringEvent);
        }

        private static LocalDate parseDate(String rawDate) {
            String normalizedDate = rawDate == null ? "" : rawDate.trim().replace(" ", "");
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(normalizedDate, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
            throw new BusinessRuleException("date must use yyyy-MM-dd, MM/dd/yyyy, or dd/MM/yyyy");
        }

        private static LocalTime parseTime(String rawTime) {
            String normalizedTime = rawTime == null ? "" : rawTime.trim().toUpperCase(Locale.ENGLISH);
            for (DateTimeFormatter formatter : TIME_FORMATTERS) {
                try {
                    return LocalTime.parse(normalizedTime, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
            throw new BusinessRuleException("time must use HH:mm or h:mm a format");
        }

        private static String normalizeTags(String rawTags) {
            if (isBlank(rawTags)) {
                return null;
            }

            String normalized = StreamSupport.stream(List.of(rawTags.split(",")).spliterator(), false)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");

            return normalized.isBlank() ? null : normalized;
        }

        private static boolean isBlank(String value) {
            return value == null || value.isBlank();
        }
    }

    public record AdminActorRequest(UUID adminId) {
    }

    public record AdminNotifyResidentsRequest(
            String title,
            String message,
            NotificationChannel channel
    ) {
    }
}
