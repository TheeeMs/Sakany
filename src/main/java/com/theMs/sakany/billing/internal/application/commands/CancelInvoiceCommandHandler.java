package com.theMs.sakany.billing.internal.application.commands;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelInvoiceCommandHandler implements CommandHandler<CancelInvoiceCommand, Void> {

    private final InvoiceRepository invoiceRepository;

    public CancelInvoiceCommandHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public Void handle(CancelInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new NotFoundException("Invoice", command.invoiceId()));

        invoice.cancel();
        invoiceRepository.save(invoice);
        return null;
    }
}
