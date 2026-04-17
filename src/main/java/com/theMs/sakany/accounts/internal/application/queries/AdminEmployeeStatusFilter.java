package com.theMs.sakany.accounts.internal.application.queries;

import java.util.Locale;

public enum AdminEmployeeStatusFilter {
    ALL,
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public static AdminEmployeeStatusFilter from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return ALL;
        }

        String normalized = rawValue
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "ALL" -> ALL;
            case "ACTIVE" -> ACTIVE;
            case "INACTIVE" -> INACTIVE;
            case "SUSPENDED" -> SUSPENDED;
            default -> throw new IllegalArgumentException("Unsupported status filter: " + rawValue);
        };
    }
}
