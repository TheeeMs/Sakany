package com.theMs.sakany.community.internal.application.queries;

import java.util.Locale;

public enum AdminMissingFoundStatusFilter {
    ALL,
    OPEN,
    MATCHED,
    RESOLVED;

    public static AdminMissingFoundStatusFilter from(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "OPEN" -> OPEN;
            case "MATCHED" -> MATCHED;
            case "RESOLVED" -> RESOLVED;
            default -> ALL;
        };
    }
}