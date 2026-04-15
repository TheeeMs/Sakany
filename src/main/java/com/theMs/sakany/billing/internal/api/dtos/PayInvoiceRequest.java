package com.theMs.sakany.billing.internal.api.dtos;

import com.theMs.sakany.billing.internal.domain.PaymentMethod;

import java.util.UUID;

public record PayInvoiceRequest(
        UUID residentId,
        PaymentMethod paymentMethod,
        String transactionReference
) {
}
