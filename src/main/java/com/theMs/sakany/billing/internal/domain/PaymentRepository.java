package com.theMs.sakany.billing.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(UUID id);

    List<Payment> findAll();

    List<Payment> findByResidentId(UUID residentId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByResidentIdAndStatus(UUID residentId, PaymentStatus status);
}
