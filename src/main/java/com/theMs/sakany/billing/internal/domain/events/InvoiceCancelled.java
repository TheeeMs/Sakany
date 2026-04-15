package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvoiceCancelled(UUID invoiceId, Instant occurredAt) implements DomainEvent {
    public InvoiceCancelled(UUID invoiceId) {
        this(invoiceId, Instant.now());
    }
}
