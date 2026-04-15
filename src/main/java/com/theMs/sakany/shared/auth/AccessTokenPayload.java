package com.theMs.sakany.shared.auth;

import com.theMs.sakany.accounts.internal.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record AccessTokenPayload(
        UUID userId,
        Role role,
        Instant issuedAt,
        Instant expiresAt
) {
}
