package com.theMs.sakany.billing.internal.application.commands;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IssueInvoiceCommandHandler implements CommandHandler<IssueInvoiceCommand, UUID> {

    private final InvoiceRepository invoiceRepository;

    public IssueInvoiceCommandHandler(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public UUID handle(IssueInvoiceCommand command) {
        Invoice invoice = Invoice.create(
                command.residentId(),
                command.unitId(),
                command.type(),
                command.amount(),
                command.currency(),
                command.description(),
                command.dueDate()
        );

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return savedInvoice.getId();
    }
}
