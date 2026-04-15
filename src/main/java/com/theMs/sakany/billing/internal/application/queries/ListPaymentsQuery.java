package com.theMs.sakany.billing.internal.application.queries;

import com.theMs.sakany.billing.internal.domain.Payment;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record ListPaymentsQuery(
        UUID residentId,
        PaymentStatus status
) implements Query<List<Payment>> {
}
