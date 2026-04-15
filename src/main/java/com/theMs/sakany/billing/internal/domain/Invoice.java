package com.theMs.sakany.billing.internal.domain;

import com.theMs.sakany.billing.internal.domain.events.InvoiceCancelled;
import com.theMs.sakany.billing.internal.domain.events.InvoiceIssued;
import com.theMs.sakany.billing.internal.domain.events.InvoiceOverdue;
import com.theMs.sakany.billing.internal.domain.events.InvoicePaid;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Invoice extends AggregateRoot {
    private UUID id;
    private UUID residentId;
    private UUID unitId;
    private InvoiceType type;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private Instant issuedAt;
    private Instant paidAt;

    private Invoice(
            UUID id,
            UUID residentId,
            UUID unitId,
            InvoiceType type,
            BigDecimal amount,
            String currency,
            String description,
            LocalDate dueDate,
            InvoiceStatus status,
            Instant issuedAt,
            Instant paidAt
    ) {
        this.id = id;
        this.residentId = residentId;
        this.unitId = unitId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
    }

    public static Invoice create(
            UUID residentId,
            UUID unitId,
            InvoiceType type,
            BigDecimal amount,
            String currency,
            String description,
            LocalDate dueDate
    ) {
        if (residentId == null) {
            throw new BusinessRuleException("Invoice residentId cannot be null");
        }
        if (unitId == null) {
            throw new BusinessRuleException("Invoice unitId cannot be null");
        }
        if (type == null) {
            throw new BusinessRuleException("Invoice type cannot be null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Invoice amount must be positive");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new BusinessRuleException("Invoice description cannot be null or empty");
        }
        if (dueDate == null) {
            throw new BusinessRuleException("Invoice dueDate cannot be null");
        }

        String effectiveCurrency = (currency == null || currency.trim().isEmpty()) ? "EGP" : currency.trim().toUpperCase();

        UUID id = UUID.randomUUID();
        Instant issuedAt = Instant.now();
        Invoice invoice = new Invoice(
                id,
                residentId,
                unitId,
                type,
                amount,
                effectiveCurrency,
                description,
                dueDate,
                InvoiceStatus.PENDING,
                issuedAt,
                null
        );
        invoice.registerEvent(new InvoiceIssued(id, residentId, unitId, amount, effectiveCurrency, dueDate, issuedAt));
        return invoice;
    }

    public void pay() {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot pay an invoice that is already paid or cancelled");
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = Instant.now();
        registerEvent(new InvoicePaid(id, paidAt));
    }

    public void cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new BusinessRuleException("Cannot cancel a paid invoice");
        }
        if (status == InvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("Invoice is already cancelled");
        }
        this.status = InvoiceStatus.CANCELLED;
        registerEvent(new InvoiceCancelled(id));
    }

    public void markOverdue() {
        if (status != InvoiceStatus.PENDING) {
            return;
        }
        if (!dueDate.isBefore(LocalDate.now())) {
            return;
        }
        this.status = InvoiceStatus.OVERDUE;
        registerEvent(new InvoiceOverdue(id, dueDate));
    }

    public UUID getId() {
        return id;
    }

    public UUID getResidentId() {
        return residentId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public InvoiceType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
