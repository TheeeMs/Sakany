package com.theMs.sakany.community.internal.application.queries;

import java.util.Locale;

public enum AdminMissingFoundTypeFilter {
    ALL,
    MISSING,
    FOUND;

    public static AdminMissingFoundTypeFilter from(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "MISSING" -> MISSING;
            case "FOUND" -> FOUND;
            default -> ALL;
        };
    }
}