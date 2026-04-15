package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvoicePaid(UUID invoiceId, Instant occurredAt) implements DomainEvent {
    public InvoicePaid(UUID invoiceId) {
        this(invoiceId, Instant.now());
    }
}
