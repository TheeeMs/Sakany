package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.Compound;
import com.theMs.sakany.property.internal.domain.CompoundRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CompoundRepositoryImpl implements CompoundRepository {
    private final CompoundJpaRepository jpaRepository;
    private final CompoundMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public CompoundRepositoryImpl(CompoundJpaRepository jpaRepository, CompoundMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Compound save(Compound compound) {
        CompoundEntity entity = mapper.toEntity(compound);
        CompoundEntity savedEntity = jpaRepository.save(entity);

        compound.getDomainEvents().forEach(eventPublisher::publishEvent);
        compound.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Compound> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
