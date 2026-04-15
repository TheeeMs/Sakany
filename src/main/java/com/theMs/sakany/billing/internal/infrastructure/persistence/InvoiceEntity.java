package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import com.theMs.sakany.billing.internal.domain.InvoiceType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class InvoiceEntity extends BaseEntity {

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private InvoiceType type;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    protected InvoiceEntity() {
    }

    public InvoiceEntity(
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
