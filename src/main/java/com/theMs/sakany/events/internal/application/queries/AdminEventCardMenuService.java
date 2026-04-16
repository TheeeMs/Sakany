package com.theMs.sakany.events.internal.application.queries;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileJpaRepository;
import com.theMs.sakany.events.internal.application.commands.CancelEventCommand;
import com.theMs.sakany.events.internal.application.commands.CancelEventCommandHandler;
import com.theMs.sakany.events.internal.infrastructure.persistence.CommunityEventEntity;
import com.theMs.sakany.events.internal.infrastructure.persistence.CommunityEventJpaRepository;
import com.theMs.sakany.events.internal.infrastructure.persistence.EventAttendeeRow;
import com.theMs.sakany.events.internal.infrastructure.persistence.EventRegistrationJpaRepository;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommand;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommandHandler;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitJpaRepository;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Service
public class AdminEventCardMenuService {

    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter EVENT_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private final CommunityEventJpaRepository communityEventJpaRepository;
    private final EventRegistrationJpaRepository eventRegistrationJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ResidentProfileJpaRepository residentProfileJpaRepository;
    private final UnitJpaRepository unitJpaRepository;
    private final BuildingJpaRepository buildingJpaRepository;
    private final SendNotificationCommandHandler sendNotificationCommandHandler;
    private final CancelEventCommandHandler cancelEventCommandHandler;

    public AdminEventCardMenuService(
            CommunityEventJpaRepository communityEventJpaRepository,
            EventRegistrationJpaRepository eventRegistrationJpaRepository,
            UserJpaRepository userJpaRepository,
            ResidentProfileJpaRepository residentProfileJpaRepository,
            UnitJpaRepository unitJpaRepository,
            BuildingJpaRepository buildingJpaRepository,
            SendNotificationCommandHandler sendNotificationCommandHandler,
            CancelEventCommandHandler cancelEventCommandHandler
    ) {
        this.communityEventJpaRepository = communityEventJpaRepository;
        this.eventRegistrationJpaRepository = eventRegistrationJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.residentProfileJpaRepository = residentProfileJpaRepository;
        this.unitJpaRepository = unitJpaRepository;
        this.buildingJpaRepository = buildingJpaRepository;
        this.sendNotificationCommandHandler = sendNotificationCommandHandler;
        this.cancelEventCommandHandler = cancelEventCommandHandler;
    }

    @Transactional(readOnly = true)
    public AdminEventCardDetailsResponse getDetails(UUID eventId) {
        CommunityEventEntity event = communityEventJpaRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("CommunityEvent", eventId));

        List<EventAttendeeRow> attendees = eventRegistrationJpaRepository.findRegisteredAttendees(eventId);
        String organizerName = resolveOrganizerName(event);
        OrganizerContext organizerContext = resolveOrganizerContext(event);
        String approvedByName = resolveApprovedByName(event);
        Instant approvedAt = resolveApprovedAt(event);
        List<EventTimelineItem> timeline = buildTimeline(event, organizerName, approvedByName, approvedAt);
        String approvalNote = approvedByName == null || approvedAt == null
            ? null
            : "Approved by " + approvedByName + " on " + EXPORT_DATE_FORMAT.format(approvedAt.atOffset(ZoneOffset.UTC));

        return new AdminEventCardDetailsResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getCategory(),
                event.getImageUrl(),
                event.getStartDate(),
                event.getEndDate(),
                event.getTags(),
                event.isRecurringEvent(),
                event.getStatus().name(),
                mapUiStatus(event),
                event.getCurrentAttendees(),
                event.getMaxAttendees(),
                calculateOccupancyPercent(event.getCurrentAttendees(), event.getMaxAttendees()),
                organizerName,
                event.getHostName(),
                event.getHostRole(),
                event.getContactPhone(),
                organizerContext.phone(),
                organizerContext.unitNumber(),
                organizerContext.buildingName(),
                formatTimeAndDuration(event.getStartDate(), event.getEndDate()),
                parseTags(event.getTags()),
                resolveGalleryImages(event),
                approvedByName,
                approvedAt,
                approvalNote,
                timeline,
                attendees.stream().map(this::mapAttendee).toList(),
                isApprovable(event),
                isRejectable(event),
                isEditable(event),
                isDeletable(event),
                canNotifyResidents(event),
                canExportAttendees(event),
                "/v1/admin/events/" + eventId + "/approve",
                "/v1/admin/events/" + eventId + "/reject",
                "/v1/admin/events/" + eventId,
                "/v1/admin/events/" + eventId,
                "/v1/admin/events/" + eventId + "/notify-residents",
                "/v1/admin/events/" + eventId + "/attendees/export",
                "/v1/admin/events/" + eventId
        );
    }

    @Transactional
    public void editEvent(UUID eventId, AdminUpdateEventRequest request) {
        CommunityEventEntity event = communityEventJpaRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("CommunityEvent", eventId));

        if (!isEditable(event)) {
            throw new BusinessRuleException("Event cannot be edited in its current status");
        }

        Instant effectiveStart = request.resolveStartDate(event.getStartDate());
        Instant effectiveEnd = request.resolveEndDate(effectiveStart, event.getEndDate());
        if (effectiveStart == null) {
            throw new BusinessRuleException("startDate is required");
        }
        if (effectiveEnd != null && effectiveEnd.isBefore(effectiveStart)) {
            throw new BusinessRuleException("endDate must be after startDate");
        }

        if (request.maxAttendees() != null && request.maxAttendees() < event.getCurrentAttendees()) {
            throw new BusinessRuleException("maxAttendees cannot be less than current attendees");
        }

        if (request.title() != null) {
            event.setTitle(request.title());
        }
        if (request.description() != null) {
            event.setDescription(request.description());
        }
        if (request.location() != null) {
            event.setLocation(request.location());
        }
        if (!Objects.equals(event.getStartDate(), effectiveStart)) {
            event.setStartDate(effectiveStart);
        }
        if (!Objects.equals(event.getEndDate(), effectiveEnd)) {
            event.setEndDate(effectiveEnd);
        }
        if (request.imageUrl() != null) {
            event.setImageUrl(request.imageUrl());
        }
        if (request.hostName() != null) {
            event.setHostName(request.hostName());
        }
        if (request.price() != null) {
            event.setPrice(request.price());
        }
        if (request.maxAttendees() != null) {
            event.setMaxAttendees(request.maxAttendees());
        }
        if (request.category() != null) {
            event.setCategory(request.category());
        }
        if (request.hostRole() != null) {
            event.setHostRole(request.hostRole());
        }
        if (request.contactPhone() != null) {
            event.setContactPhone(request.resolveContactPhone());
        }
        if (request.latitude() != null) {
            event.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            event.setLongitude(request.longitude());
        }
        if (request.tags() != null) {
            event.setTags(request.resolveTags());
        }
        if (request.recurringEvent() != null) {
            event.setRecurringEvent(request.recurringEvent());
        }

        communityEventJpaRepository.save(event);
    }

    @Transactional
    public NotifyResidentsResult notifyResidents(UUID eventId, NotifyResidentsRequest request) {
        CommunityEventEntity event = communityEventJpaRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("CommunityEvent", eventId));

        List<EventAttendeeRow> attendees = eventRegistrationJpaRepository.findRegisteredAttendees(eventId);
        String title = request.title() != null && !request.title().isBlank()
                ? request.title().trim()
                : "Event Update: " + event.getTitle();
        String message = request.message() != null && !request.message().isBlank()
                ? request.message().trim()
                : buildDefaultEventReminderMessage(event);
        NotificationChannel channel = request.channel() == null ? NotificationChannel.IN_APP : request.channel();

        for (EventAttendeeRow attendee : attendees) {
            sendNotificationCommandHandler.handle(new SendNotificationCommand(
                    attendee.getResidentId(),
                    title,
                    message,
                    NotificationType.EVENT_REMINDER,
                    eventId,
                    channel
            ));
        }

        return new NotifyResidentsResult(eventId, attendees.size(), title, channel.name());
    }

    @Transactional(readOnly = true)
    public byte[] exportAttendees(UUID eventId) {
        CommunityEventEntity event = communityEventJpaRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("CommunityEvent", eventId));

        List<EventAttendeeRow> attendees = eventRegistrationJpaRepository.findRegisteredAttendees(eventId);

        StringBuilder csv = new StringBuilder();
        csv.append("Event ID,Event Title\n");
        csv.append(escapeCsv(event.getId().toString())).append(',')
                .append(escapeCsv(event.getTitle())).append("\n\n");
        csv.append("Resident ID,Full Name,Phone,Email,Registered At\n");

        for (EventAttendeeRow attendee : attendees) {
            String fullName = (safe(attendee.getFirstName()) + " " + safe(attendee.getLastName())).trim();
            String registeredAt = attendee.getRegisteredAt() == null
                    ? ""
                    : EXPORT_DATE_FORMAT.format(attendee.getRegisteredAt().atOffset(ZoneOffset.UTC));

            csv.append(escapeCsv(attendee.getResidentId() == null ? "" : attendee.getResidentId().toString())).append(',')
                    .append(escapeCsv(fullName)).append(',')
                    .append(escapeCsv(attendee.getPhoneNumber())).append(',')
                    .append(escapeCsv(attendee.getEmail())).append(',')
                    .append(escapeCsv(registeredAt)).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void deleteEvent(UUID eventId, UUID adminId) {
        cancelEventCommandHandler.handle(new CancelEventCommand(eventId, adminId));
    }

    private EventAttendeeDetails mapAttendee(EventAttendeeRow row) {
        return new EventAttendeeDetails(
                row.getResidentId(),
                (safe(row.getFirstName()) + " " + safe(row.getLastName())).trim(),
                row.getPhoneNumber(),
                row.getEmail(),
                row.getRegisteredAt()
        );
    }

    private String resolveOrganizerName(CommunityEventEntity event) {
        if (event.getOrganizerId() != null) {
            UserEntity organizer = userJpaRepository.findById(event.getOrganizerId()).orElse(null);
            if (organizer != null) {
                String organizerName = (safe(organizer.getFirstName()) + " " + safe(organizer.getLastName())).trim();
                if (!organizerName.isEmpty()) {
                    return organizerName;
                }
            }
        }

        if (event.getHostName() != null && !event.getHostName().isBlank()) {
            return event.getHostName();
        }
        return "Unknown Organizer";
    }

    private boolean isEditable(CommunityEventEntity event) {
        return switch (event.getStatus()) {
            case COMPLETED, CANCELLED -> false;
            default -> true;
        };
    }

    private boolean isApprovable(CommunityEventEntity event) {
        return "PROPOSED".equals(event.getStatus().name());
    }

    private boolean isRejectable(CommunityEventEntity event) {
        return "PROPOSED".equals(event.getStatus().name());
    }

    private boolean isDeletable(CommunityEventEntity event) {
        return switch (event.getStatus()) {
            case CANCELLED, COMPLETED -> false;
            default -> true;
        };
    }

    private boolean canNotifyResidents(CommunityEventEntity event) {
        return "APPROVED".equals(event.getStatus().name());
    }

    private boolean canExportAttendees(CommunityEventEntity event) {
        return switch (event.getStatus().name()) {
            case "PROPOSED", "REJECTED" -> false;
            default -> true;
        };
    }

    private OrganizerContext resolveOrganizerContext(CommunityEventEntity event) {
        if (event.getOrganizerId() == null) {
            return new OrganizerContext(event.getContactPhone(), null, null);
        }

        UserEntity organizer = userJpaRepository.findById(event.getOrganizerId()).orElse(null);
        String organizerPhone = organizer != null ? organizer.getPhone() : null;

        ResidentProfileEntity profile = residentProfileJpaRepository.findByUserId(event.getOrganizerId()).orElse(null);
        if (profile == null || profile.getUnitId() == null) {
            return new OrganizerContext(organizerPhone, null, null);
        }

        UnitEntity unit = unitJpaRepository.findById(profile.getUnitId()).orElse(null);
        BuildingEntity building = unit == null ? null : buildingJpaRepository.findById(unit.getBuildingId()).orElse(null);

        return new OrganizerContext(
                organizerPhone,
                unit != null ? unit.getUnitNumber() : null,
                building != null ? building.getName() : null
        );
    }

    private String resolveApprovedByName(CommunityEventEntity event) {
        if (event.getApprovedBy() == null) {
            return null;
        }

        UserEntity approver = userJpaRepository.findById(event.getApprovedBy()).orElse(null);
        if (approver == null) {
            return null;
        }

        String fullName = (safe(approver.getFirstName()) + " " + safe(approver.getLastName())).trim();
        return fullName.isBlank() ? null : fullName;
    }

    private Instant resolveApprovedAt(CommunityEventEntity event) {
        if (event.getApprovedBy() == null) {
            return null;
        }
        return event.getUpdatedAt();
    }

    private List<EventTimelineItem> buildTimeline(
            CommunityEventEntity event,
            String organizerName,
            String approvedByName,
            Instant approvedAt
    ) {
        List<EventTimelineItem> timeline = new ArrayList<>();

        Instant createdAt = event.getCreatedAt();
        if (createdAt != null) {
            timeline.add(new EventTimelineItem("Event Created", createdAt, organizerName, null));
            timeline.add(new EventTimelineItem("Event Submitted for Approval", createdAt, organizerName, null));
        }

        if (approvedByName != null && approvedAt != null) {
            timeline.add(new EventTimelineItem("Event Approved", approvedAt, approvedByName, "All requirements met"));
        }

        if ("REJECTED".equals(event.getStatus().name())) {
            timeline.add(new EventTimelineItem("Event Rejected", event.getUpdatedAt(), null, null));
        }

        if ("COMPLETED".equals(event.getStatus().name())) {
            timeline.add(new EventTimelineItem("Event Completed", event.getUpdatedAt(), null, null));
        }

        if ("CANCELLED".equals(event.getStatus().name())) {
            timeline.add(new EventTimelineItem("Event Cancelled", event.getUpdatedAt(), null, null));
        }

        timeline.sort(Comparator.comparing(EventTimelineItem::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())));
        return timeline;
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }

        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<String> resolveGalleryImages(CommunityEventEntity event) {
        if (event.getImageUrl() == null || event.getImageUrl().isBlank()) {
            return List.of();
        }
        return List.of(event.getImageUrl());
    }

    private String formatTimeAndDuration(Instant startDate, Instant endDate) {
        if (startDate == null) {
            return null;
        }

        String time = EVENT_TIME_FORMAT.format(startDate);
        if (endDate == null || endDate.isBefore(startDate)) {
            return time;
        }

        Duration duration = Duration.between(startDate, endDate);
        long hours = duration.toHours();
        if (hours > 0 && duration.minusHours(hours).isZero()) {
            return time + " • " + hours + (hours == 1 ? " Hour" : " Hours");
        }

        long minutes = duration.toMinutes();
        return time + " • " + minutes + " Min";
    }

    private String mapUiStatus(CommunityEventEntity event) {
        return switch (event.getStatus()) {
            case PROPOSED -> "PENDING";
            case REJECTED -> "REJECTED";
            case COMPLETED, CANCELLED -> "COMPLETED";
            case APPROVED -> {
                if (event.getStartDate() != null
                        && event.getStartDate().isBefore(Instant.now())
                        && (event.getEndDate() == null || event.getEndDate().isAfter(Instant.now()))) {
                    yield "ONGOING";
                }
                if (event.getEndDate() != null && event.getEndDate().isBefore(Instant.now())) {
                    yield "COMPLETED";
                }
                yield "APPROVED";
            }
        };
    }

    private int calculateOccupancyPercent(int currentAttendees, Integer maxAttendees) {
        if (maxAttendees == null || maxAttendees <= 0) {
            return 0;
        }
        return (int) Math.round((currentAttendees * 100.0) / maxAttendees);
    }

    private String buildDefaultEventReminderMessage(CommunityEventEntity event) {
        String when = event.getStartDate() == null
                ? "soon"
                : EXPORT_DATE_FORMAT.format(event.getStartDate().atOffset(ZoneOffset.UTC));
        return "Reminder: " + event.getTitle() + " starts at " + when + " in " + safe(event.getLocation()) + ".";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeCsv(String value) {
        String safe = value == null ? "" : value;
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    public record AdminEventCardDetailsResponse(
            UUID eventId,
            String title,
            String description,
            String location,
            String category,
            String imageUrl,
            Instant startDate,
            Instant endDate,
            String tags,
            boolean recurringEvent,
            String workflowStatus,
            String uiStatus,
            int currentAttendees,
            Integer maxAttendees,
            int occupancyPercent,
            String organizerName,
            String hostName,
            String hostRole,
            String contactPhone,
                String organizerPhone,
                String organizerUnitNumber,
                String organizerBuildingName,
                String timeAndDuration,
                List<String> tagList,
                List<String> galleryImages,
                String approvedByName,
                Instant approvedAt,
                String approvalNote,
                List<EventTimelineItem> activityTimeline,
            List<EventAttendeeDetails> attendees,
            boolean canApprove,
            boolean canReject,
            boolean canEdit,
            boolean canDelete,
                boolean canNotifyResidents,
                boolean canExportAttendees,
            String approveUrl,
            String rejectUrl,
            String viewDetailsUrl,
            String editUrl,
            String notifyResidentsUrl,
            String exportAttendeesUrl,
            String deleteUrl
    ) {
    }

            public record EventTimelineItem(
                String title,
                Instant occurredAt,
                String actor,
                String details
            ) {
            }

    public record EventAttendeeDetails(
            UUID residentId,
            String fullName,
            String phone,
            String email,
            Instant registeredAt
    ) {
    }

    public record AdminUpdateEventRequest(
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
            JsonNode tags,
            @JsonAlias({"isRecurring"})
            Boolean recurringEvent,
            String date,
            String time,
            Integer duration,
            String durationUnit
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

        Instant resolveStartDate(Instant existingStartDate) {
            if (startDate != null) {
                return startDate;
            }

            if (isBlank(date) && isBlank(time)) {
                return existingStartDate;
            }
            if (isBlank(date)) {
                throw new BusinessRuleException("date is required when using time in edit payload");
            }
            if (isBlank(time)) {
                throw new BusinessRuleException("time is required when using date in edit payload");
            }

            LocalDate parsedDate = parseDate(date);
            LocalTime parsedTime = parseTime(time);
            LocalDateTime dateTime = LocalDateTime.of(parsedDate, parsedTime);
            return dateTime.toInstant(ZoneOffset.UTC);
        }

        Instant resolveEndDate(Instant effectiveStartDate, Instant existingEndDate) {
            if (endDate != null) {
                return endDate;
            }

            if (duration == null) {
                return existingEndDate;
            }

            if (effectiveStartDate == null) {
                throw new BusinessRuleException("startDate is required when duration is provided");
            }
            if (duration <= 0) {
                throw new BusinessRuleException("duration must be greater than 0");
            }

            String normalizedUnit = isBlank(durationUnit) ? "HOUR" : durationUnit.trim().toUpperCase(Locale.ROOT);
            Duration effectiveDuration = switch (normalizedUnit) {
                case "HOUR", "HOURS" -> Duration.ofHours(duration.longValue());
                case "DAY", "DAYS" -> Duration.ofDays(duration.longValue());
                case "MIN", "MINS", "MINUTE", "MINUTES" -> Duration.ofMinutes(duration.longValue());
                default -> throw new BusinessRuleException("durationUnit must be HOUR, DAY, or MINUTE");
            };

            return effectiveStartDate.plus(effectiveDuration);
        }

        String resolveContactPhone() {
            if (isBlank(contactPhone)) {
                return null;
            }

            String trimmed = contactPhone.trim();
            if (trimmed.startsWith("00") && trimmed.length() > 2) {
                return "+" + trimmed.substring(2);
            }
            return trimmed;
        }

        String resolveTags() {
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

            String normalized = Arrays.stream(rawTags.split(","))
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

    public record NotifyResidentsRequest(
            String title,
            String message,
            NotificationChannel channel
    ) {
    }

    public record NotifyResidentsResult(
            UUID eventId,
            int notifiedResidents,
            String title,
            String channel
    ) {
    }

        private record OrganizerContext(
            String phone,
            String unitNumber,
            String buildingName
        ) {
        }
}
