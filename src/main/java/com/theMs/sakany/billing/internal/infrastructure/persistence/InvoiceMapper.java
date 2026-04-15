package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import com.theMs.sakany.billing.internal.domain.InvoiceType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class InvoiceMapper {

    public InvoiceEntity toEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceEntity entity = new InvoiceEntity(
                invoice.getResidentId(),
                invoice.getUnitId(),
                invoice.getType(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getDescription(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getPaidAt()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, invoice.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on InvoiceEntity", e);
        }

        return entity;
    }

    public Invoice toDomain(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<Invoice> constructor = Invoice.class.getDeclaredConstructor(
                    UUID.class,
                    UUID.class,
                    UUID.class,
                    InvoiceType.class,
                    BigDecimal.class,
                    String.class,
                    String.class,
                    LocalDate.class,
                    InvoiceStatus.class,
                    Instant.class,
                    Instant.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(),
                    entity.getResidentId(),
                    entity.getUnitId(),
                    entity.getType(),
                    entity.getAmount(),
                    entity.getCurrency(),
                    entity.getDescription(),
                    entity.getDueDate(),
                    entity.getStatus(),
                    entity.getIssuedAt(),
                    entity.getPaidAt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map InvoiceEntity to Invoice domain model", e);
        }
    }
}
