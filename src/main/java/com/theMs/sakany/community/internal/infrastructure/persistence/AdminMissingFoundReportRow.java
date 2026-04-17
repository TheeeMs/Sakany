package com.theMs.sakany.community.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface AdminMissingFoundReportRow {
    UUID getReportId();

    UUID getReporterId();

    String getReportType();

    String getCategory();

    String getTitle();

    String getDescription();

    String getLocation();

    Instant getEventTime();

    String getStatus();

    Instant getResolvedAt();

    String getContactNumber();

    Instant getCreatedAt();

    String getReporterName();

    String getReporterUnitLabel();
}