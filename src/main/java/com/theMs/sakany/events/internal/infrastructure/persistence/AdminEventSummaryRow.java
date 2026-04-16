package com.theMs.sakany.events.internal.infrastructure.persistence;

public interface AdminEventSummaryRow {
    Long getTotalCount();

    Long getPendingCount();

    Long getApprovedCount();

    Long getOngoingCount();

    Long getCompletedCount();

    Long getRejectedCount();
}
