package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PaymentCompleted(UUID paymentId, UUID invoiceId, Instant occurredAt) implements DomainEvent {
    public PaymentCompleted(UUID paymentId, UUID invoiceId) {
        this(paymentId, invoiceId, Instant.now());
    }
}
