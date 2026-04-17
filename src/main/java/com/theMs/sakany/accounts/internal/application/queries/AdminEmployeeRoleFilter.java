package com.theMs.sakany.accounts.internal.application.queries;

import java.util.Locale;

public enum AdminEmployeeRoleFilter {
    ALL,
    SUPER_ADMIN,
    ADMIN,
    TECHNICIAN,
    SECURITY_STAFF;

    public static AdminEmployeeRoleFilter from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return ALL;
        }

        String normalized = rawValue
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        if ("SECURITY_GUARD".equals(normalized)) {
            return SECURITY_STAFF;
        }

        return switch (normalized) {
            case "ALL" -> ALL;
            case "SUPER_ADMIN" -> SUPER_ADMIN;
            case "ADMIN" -> ADMIN;
            case "TECHNICIAN" -> TECHNICIAN;
            case "SECURITY_STAFF" -> SECURITY_STAFF;
            default -> throw new IllegalArgumentException("Unsupported role filter: " + rawValue);
        };
    }
}
