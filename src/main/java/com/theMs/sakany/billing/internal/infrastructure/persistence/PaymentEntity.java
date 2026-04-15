package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.PaymentMethod;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity extends BaseEntity {

    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    protected PaymentEntity() {
    }

    public PaymentEntity(
            UUID invoiceId,
            UUID residentId,
            BigDecimal amount,
            PaymentMethod paymentMethod,
            String transactionReference,
            PaymentStatus status
    ) {
        this.invoiceId = invoiceId;
        this.residentId = residentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.status = status;
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
