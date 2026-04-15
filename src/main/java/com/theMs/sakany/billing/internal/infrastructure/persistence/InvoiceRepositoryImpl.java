package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceRepository;
import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;
    private final InvoiceMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public InvoiceRepositoryImpl(
            InvoiceJpaRepository jpaRepository,
            InvoiceMapper mapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = Objects.requireNonNull(mapper.toEntity(invoice), "Invoice entity cannot be null");
        InvoiceEntity savedEntity = jpaRepository.save(entity);

        invoice.getDomainEvents().forEach(eventPublisher::publishEvent);
        invoice.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        Objects.requireNonNull(id, "Invoice id cannot be null");
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Invoice> findByResidentId(UUID residentId) {
        return jpaRepository.findByResidentId(residentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Invoice> findByResidentIdAndStatus(UUID residentId, InvoiceStatus status) {
        return jpaRepository.findByResidentIdAndStatus(residentId, status).stream().map(mapper::toDomain).toList();
    }
}
