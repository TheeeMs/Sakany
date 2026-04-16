package com.theMs.sakany.access.internal.api.dtos;

import com.theMs.sakany.access.internal.domain.AccessCodeStatus;
import com.theMs.sakany.access.internal.domain.VisitPurpose;

import java.time.Instant;
import java.util.UUID;

public record AccessCodeResponse(
    UUID id,
    UUID residentId,
    String visitorName,
    String visitorPhone,
    VisitPurpose purpose,
    String code,
    String qrData,
    boolean isSingleUse,
    Instant validFrom,
    Instant validUntil,
    AccessCodeStatus status,
    Instant usedAt
) {
}
