package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceIssued(
        UUID invoiceId,
        UUID residentId,
        UUID unitId,
        BigDecimal amount,
        String currency,
        LocalDate dueDate,
        Instant occurredAt
) implements DomainEvent {
    public InvoiceIssued(
            UUID invoiceId,
            UUID residentId,
            UUID unitId,
            BigDecimal amount,
            String currency,
            LocalDate dueDate
    ) {
        this(invoiceId, residentId, unitId, amount, currency, dueDate, Instant.now());
    }
}
