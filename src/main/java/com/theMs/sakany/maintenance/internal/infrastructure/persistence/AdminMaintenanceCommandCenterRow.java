package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface AdminMaintenanceCommandCenterRow {
    UUID getRequestId();

    Boolean getPublicRequest();

    String getPriority();

    String getCategory();

    String getIssueTitle();

    String getLocationLabel();

    String getUnitNumber();

    String getBuildingName();

    Instant getRequestedAt();

    String getWorkflowStatus();

    UUID getTechnicianId();
}
