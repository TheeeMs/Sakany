package com.theMs.sakany.billing.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record CancelInvoiceCommand(UUID invoiceId) implements Command<Void> {
}
