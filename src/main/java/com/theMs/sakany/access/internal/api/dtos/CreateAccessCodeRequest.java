package com.theMs.sakany.access.internal.api.dtos;

import com.theMs.sakany.access.internal.domain.VisitPurpose;

import java.time.Instant;

public record CreateAccessCodeRequest(
    String visitorName,
    String visitorPhone,
    VisitPurpose purpose,
    boolean isSingleUse,
    Instant validFrom,
    Instant validUntil
) {
}
