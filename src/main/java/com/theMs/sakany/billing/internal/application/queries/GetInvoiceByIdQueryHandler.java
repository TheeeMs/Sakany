package com.theMs.sakany.billing.internal.application.queries;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetInvoiceByIdQueryHandler implements QueryHandler<GetInvoiceByIdQuery, Invoice> {

    private final InvoiceRepository invoiceRepository;

    public GetInvoiceByIdQueryHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Invoice handle(GetInvoiceByIdQuery query) {
        return invoiceRepository.findById(query.invoiceId())
                .orElseThrow(() -> new NotFoundException("Invoice", query.invoiceId()));
    }
}
