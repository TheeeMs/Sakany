package com.theMs.sakany.access.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface AdminQrAccessRow {
    UUID getAccessCodeId();

    String getAccessCode();

    UUID getResidentId();

    String getResidentFirstName();

    String getResidentLastName();

    String getVisitorName();

    String getVisitorPhone();

    String getPurpose();

    String getUnitNumber();

    Instant getCreatedAt();

    Instant getValidUntil();

    String getEffectiveStatus();
}
