package com.theMs.sakany.billing.internal.application.commands;

import com.theMs.sakany.billing.internal.domain.PaymentMethod;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record PayInvoiceCommand(
        UUID invoiceId,
        UUID residentId,
        PaymentMethod paymentMethod,
        String transactionReference
) implements Command<UUID> {
}
