package com.theMs.sakany.events.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface EventAttendeeRow {
    UUID getResidentId();

    String getFirstName();

    String getLastName();

    String getPhoneNumber();

    String getEmail();

    Instant getRegisteredAt();
}
