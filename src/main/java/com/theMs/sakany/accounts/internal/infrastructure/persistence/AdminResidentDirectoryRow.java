package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface AdminResidentDirectoryRow {
    UUID getResidentId();

    UUID getProfileId();

    String getFirstName();

    String getLastName();

    String getPhoneNumber();

    String getEmail();

    Boolean getActive();

    Boolean getPhoneVerified();

    String getApprovalStatus();

    String getResidentType();

    LocalDate getMoveInDate();

    UUID getUnitId();

    String getUnitNumber();

    UUID getBuildingId();

    String getBuildingName();

    BigDecimal getDueAmount();

    String getCurrency();

    Instant getCreatedAt();
}
