package com.theMs.sakany.billing.internal.application.commands;

import com.theMs.sakany.billing.internal.domain.InvoiceType;
import com.theMs.sakany.shared.cqrs.Command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IssueInvoiceCommand(
        UUID residentId,
        UUID unitId,
        InvoiceType type,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate dueDate
) implements Command<UUID> {
}
