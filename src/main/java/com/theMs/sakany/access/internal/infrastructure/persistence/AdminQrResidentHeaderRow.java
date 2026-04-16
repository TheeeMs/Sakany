package com.theMs.sakany.access.internal.infrastructure.persistence;

import java.util.UUID;

public interface AdminQrResidentHeaderRow {
    UUID getResidentId();

    String getFirstName();

    String getLastName();

    String getPhoneNumber();

    String getUnitNumber();

    String getBuildingName();
}
