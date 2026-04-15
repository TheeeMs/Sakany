package com.theMs.sakany.billing.internal.application.queries;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.UUID;

public record GetInvoiceByIdQuery(UUID invoiceId) implements Query<Invoice> {
}
