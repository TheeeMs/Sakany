package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.Payment;
import com.theMs.sakany.billing.internal.domain.PaymentMethod;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentMapper {

    public PaymentEntity toEntity(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentEntity entity = new PaymentEntity(
                payment.getInvoiceId(),
                payment.getResidentId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getStatus()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, payment.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on PaymentEntity", e);
        }

        return entity;
    }

    public Payment toDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<Payment> constructor = Payment.class.getDeclaredConstructor(
                    UUID.class,
                    UUID.class,
                    UUID.class,
                    BigDecimal.class,
                    PaymentMethod.class,
                    String.class,
                    PaymentStatus.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(),
                    entity.getInvoiceId(),
                    entity.getResidentId(),
                    entity.getAmount(),
                    entity.getPaymentMethod(),
                    entity.getTransactionReference(),
                    entity.getStatus()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map PaymentEntity to Payment domain model", e);
        }
    }
}
