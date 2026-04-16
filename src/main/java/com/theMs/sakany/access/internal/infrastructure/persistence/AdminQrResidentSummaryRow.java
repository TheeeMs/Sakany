package com.theMs.sakany.access.internal.infrastructure.persistence;

public interface AdminQrResidentSummaryRow {
    Long getTotalCount();

    Long getGuestCount();

    Long getDeliveryCount();

    Long getServiceCount();

    Long getFamilyCount();

    Long getOtherCount();

    Long getActiveCount();

    Long getUsedCount();

    Long getExpiredCount();

    Long getRevokedCount();
}
