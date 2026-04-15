package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findByResidentId(UUID residentId);

    List<InvoiceEntity> findByStatus(InvoiceStatus status);

    List<InvoiceEntity> findByResidentIdAndStatus(UUID residentId, InvoiceStatus status);
}
