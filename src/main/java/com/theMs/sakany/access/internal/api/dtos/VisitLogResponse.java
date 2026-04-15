package com.theMs.sakany.access.internal.api.dtos;

import java.time.Instant;
import java.util.UUID;

public record VisitLogResponse(
    UUID id,
    UUID accessCodeId,
    UUID residentId,
    String visitorName,
    Instant entryTime,
    Instant exitTime,
    String gateNumber
) {
}
