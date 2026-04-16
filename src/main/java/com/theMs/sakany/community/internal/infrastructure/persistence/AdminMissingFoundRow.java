package com.theMs.sakany.community.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface AdminMissingFoundRow {
    UUID getReportId();

    UUID getReporterId();

    String getType();

    String getCategory();

    String getStatus();

    String getTitle();

    String getDescription();

    String getLocation();

    Instant getEventTime();

    Boolean getResolved();

    Instant getResolvedAt();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    String getReporterFirstName();

    String getReporterLastName();

    String getReporterUnitNumber();

    String getReporterBuildingName();
}
