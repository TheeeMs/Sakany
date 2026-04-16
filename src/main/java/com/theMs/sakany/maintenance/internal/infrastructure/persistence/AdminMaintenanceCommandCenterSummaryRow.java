package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

public interface AdminMaintenanceCommandCenterSummaryRow {
    Long getTotalCount();

    Long getPendingCount();

    Long getInProgressCount();

    Long getCompletedCount();
}
