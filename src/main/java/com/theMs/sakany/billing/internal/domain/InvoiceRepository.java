package com.theMs.sakany.billing.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);

    Optional<Invoice> findById(UUID id);

    List<Invoice> findAll();

    List<Invoice> findByResidentId(UUID residentId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByResidentIdAndStatus(UUID residentId, InvoiceStatus status);
}
