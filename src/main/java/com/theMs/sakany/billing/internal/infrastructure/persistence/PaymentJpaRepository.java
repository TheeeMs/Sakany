package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
    List<PaymentEntity> findByResidentId(UUID residentId);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    List<PaymentEntity> findByResidentIdAndStatus(UUID residentId, PaymentStatus status);
}
