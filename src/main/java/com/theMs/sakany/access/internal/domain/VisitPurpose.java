package com.theMs.sakany.access.internal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VisitPurpose {
    GUEST,
    DELIVERY,
    SERVICE,
    FAMILY,
    OTHER

    ;

    @JsonCreator
    public static VisitPurpose fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("purpose is required");
        }

        String normalized = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        try {
            return VisitPurpose.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
