package com.theMs.sakany.billing.internal.infrastructure.persistence;

import com.theMs.sakany.billing.internal.domain.Payment;
import com.theMs.sakany.billing.internal.domain.PaymentRepository;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentRepositoryImpl(
            PaymentJpaRepository jpaRepository,
            PaymentMapper mapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = Objects.requireNonNull(mapper.toEntity(payment), "Payment entity cannot be null");
        PaymentEntity savedEntity = jpaRepository.save(entity);

        payment.getDomainEvents().forEach(eventPublisher::publishEvent);
        payment.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        Objects.requireNonNull(id, "Payment id cannot be null");
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Payment> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Payment> findByResidentId(UUID residentId) {
        return jpaRepository.findByResidentId(residentId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return jpaRepository.findByStatus(status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Payment> findByResidentIdAndStatus(UUID residentId, PaymentStatus status) {
        return jpaRepository.findByResidentIdAndStatus(residentId, status).stream().map(mapper::toDomain).toList();
    }
}
