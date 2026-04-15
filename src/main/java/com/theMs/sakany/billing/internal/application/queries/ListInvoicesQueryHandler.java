package com.theMs.sakany.billing.internal.application.queries;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListInvoicesQueryHandler implements QueryHandler<ListInvoicesQuery, List<Invoice>> {

    private final InvoiceRepository invoiceRepository;

    public ListInvoicesQueryHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public List<Invoice> handle(ListInvoicesQuery query) {
        if (query.residentId() != null && query.status() != null) {
            return invoiceRepository.findByResidentIdAndStatus(query.residentId(), query.status());
        }
        if (query.residentId() != null) {
            return invoiceRepository.findByResidentId(query.residentId());
        }
        if (query.status() != null) {
            return invoiceRepository.findByStatus(query.status());
        }
        return invoiceRepository.findAll();
    }
}
