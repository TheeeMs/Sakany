package com.theMs.sakany.access.internal.api.dtos;

import java.time.Instant;

public record ReactivateAccessCodeRequest(
    Instant validFrom,
    Instant validUntil
) {
}