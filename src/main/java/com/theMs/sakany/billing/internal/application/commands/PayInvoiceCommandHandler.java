package com.theMs.sakany.billing.internal.application.commands;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceRepository;
import com.theMs.sakany.billing.internal.domain.Payment;
import com.theMs.sakany.billing.internal.domain.PaymentRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PayInvoiceCommandHandler implements CommandHandler<PayInvoiceCommand, UUID> {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public PayInvoiceCommandHandler(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public UUID handle(PayInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new NotFoundException("Invoice", command.invoiceId()));

        if (!invoice.getResidentId().equals(command.residentId())) {
            throw new BusinessRuleException("Only the invoice owner can pay this invoice");
        }

        invoice.pay();
        invoiceRepository.save(invoice);

        Payment payment = Payment.initiate(
                invoice.getId(),
                command.residentId(),
                invoice.getAmount(),
                command.paymentMethod(),
                command.transactionReference()
        );

        // Payment gateway integration is intentionally stubbed for now.
        payment.complete();
        Payment savedPayment = paymentRepository.save(payment);
        return savedPayment.getId();
    }
}
