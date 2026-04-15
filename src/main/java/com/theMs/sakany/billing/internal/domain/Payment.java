package com.theMs.sakany.billing.internal.domain;

import com.theMs.sakany.billing.internal.domain.events.PaymentCompleted;
import com.theMs.sakany.billing.internal.domain.events.PaymentFailed;
import com.theMs.sakany.billing.internal.domain.events.PaymentInitiated;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.UUID;

public class Payment extends AggregateRoot {
    private UUID id;
    private UUID invoiceId;
    private UUID residentId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private PaymentStatus status;

    private Payment(
            UUID id,
            UUID invoiceId,
            UUID residentId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String transactionReference,
            PaymentStatus status
    ) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.residentId = residentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.status = status;
    }

    public static Payment initiate(
            UUID invoiceId,
            UUID residentId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String transactionReference
    ) {
        if (invoiceId == null) {
            throw new BusinessRuleException("Payment invoiceId cannot be null");
        }
        if (residentId == null) {
            throw new BusinessRuleException("Payment residentId cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be positive");
        }
        if (paymentMethod == null) {
            throw new BusinessRuleException("Payment method cannot be null");
        }

        UUID id = UUID.randomUUID();
        Payment payment = new Payment(
                id,
                invoiceId,
                residentId,
                amount,
                paymentMethod,
                transactionReference,
                PaymentStatus.PENDING
        );
        payment.registerEvent(new PaymentInitiated(id, invoiceId, residentId, amount, paymentMethod));
        return payment;
    }

    public void complete() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only pending payments can be completed");
        }
        this.status = PaymentStatus.COMPLETED;
        registerEvent(new PaymentCompleted(id, invoiceId));
    }

    public void fail(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only pending payments can be marked as failed");
        }
        this.status = PaymentStatus.FAILED;
        registerEvent(new PaymentFailed(id, invoiceId, reason));
    }

    public UUID getId() {
        return id;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public UUID getResidentId() {
        return residentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
