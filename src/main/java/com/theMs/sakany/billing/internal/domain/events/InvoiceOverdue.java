package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceOverdue(UUID invoiceId, LocalDate dueDate, Instant occurredAt) implements DomainEvent {
    public InvoiceOverdue(UUID invoiceId, LocalDate dueDate) {
        this(invoiceId, dueDate, Instant.now());
    }
}
