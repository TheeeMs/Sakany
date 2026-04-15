package com.theMs.sakany.billing.internal.api.dtos;

import com.theMs.sakany.billing.internal.domain.PaymentMethod;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID invoiceId,
        UUID residentId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String transactionReference,
        PaymentStatus status
) {
}
