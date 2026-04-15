package com.theMs.sakany.billing.internal.domain.events;

import com.theMs.sakany.billing.internal.domain.PaymentMethod;
import com.theMs.sakany.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentInitiated(
        UUID paymentId,
        UUID invoiceId,
        UUID residentId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Instant occurredAt
) implements DomainEvent {
    public PaymentInitiated(
            UUID paymentId,
            UUID invoiceId,
            UUID residentId,
            BigDecimal amount,
            PaymentMethod paymentMethod
    ) {
        this(paymentId, invoiceId, residentId, amount, paymentMethod, Instant.now());
    }
}
