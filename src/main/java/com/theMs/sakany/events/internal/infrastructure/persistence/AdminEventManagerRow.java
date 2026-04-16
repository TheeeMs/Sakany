package com.theMs.sakany.events.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface AdminEventManagerRow {
    UUID getEventId();

    String getTitle();

    String getDescription();

    String getLocation();

    Instant getStartDate();

    Instant getEndDate();

    String getImageUrl();

    String getCategory();

    String getWorkflowStatus();

    String getUiStatus();

    UUID getOrganizerId();

    String getOrganizerFirstName();

    String getOrganizerLastName();

    String getHostName();

    Integer getCurrentAttendees();

    Integer getMaxAttendees();

    Instant getCreatedAt();
}
