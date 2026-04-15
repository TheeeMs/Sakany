package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailed(UUID paymentId, UUID invoiceId, String reason, Instant occurredAt) implements DomainEvent {
    public PaymentFailed(UUID paymentId, UUID invoiceId, String reason) {
        this(paymentId, invoiceId, reason, Instant.now());
    }
}
