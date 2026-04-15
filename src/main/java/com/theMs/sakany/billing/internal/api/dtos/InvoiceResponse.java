package com.theMs.sakany.billing.internal.api.dtos;

import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import com.theMs.sakany.billing.internal.domain.InvoiceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID residentId,
        UUID unitId,
        InvoiceType type,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate dueDate,
        InvoiceStatus status,
        Instant issuedAt,
        Instant paidAt
) {
}
