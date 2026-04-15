package com.theMs.sakany.billing.internal.application.queries;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record ListInvoicesQuery(
        UUID residentId,
        InvoiceStatus status
) implements Query<List<Invoice>> {
}
