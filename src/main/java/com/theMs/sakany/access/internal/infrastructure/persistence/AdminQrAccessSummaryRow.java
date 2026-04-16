package com.theMs.sakany.access.internal.infrastructure.persistence;

public interface AdminQrAccessSummaryRow {
    Long getTotalCount();

    Long getGuestCount();

    Long getDeliveryCount();

    Long getServiceCount();

    Long getFamilyCount();

    Long getOtherCount();

    Long getTodayGuestCount();

    Long getTodayDeliveryCount();

    Long getActiveQrCodes();
}
