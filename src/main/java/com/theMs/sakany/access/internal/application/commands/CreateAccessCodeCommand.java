package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import com.theMs.sakany.access.internal.domain.VisitPurpose;

import java.time.Instant;
import java.util.UUID;

public record CreateAccessCodeCommand(
    UUID residentId,
    String visitorName,
    String visitorPhone,
    VisitPurpose purpose,
    boolean isSingleUse,
    Instant validFrom,
    Instant validUntil
) implements Command {
}
