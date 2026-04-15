package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.Unit;
import com.theMs.sakany.property.internal.domain.UnitRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class UnitRepositoryImpl implements UnitRepository {
    private final UnitJpaRepository jpaRepository;
    private final UnitMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public UnitRepositoryImpl(UnitJpaRepository jpaRepository, UnitMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Unit save(Unit unit) {
        UnitEntity entity = mapper.toEntity(unit);
        UnitEntity savedEntity = jpaRepository.save(entity);

        unit.getDomainEvents().forEach(eventPublisher::publishEvent);
        unit.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Unit> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Unit> findByBuildingId(UUID buildingId) {
        return jpaRepository.findByBuildingId(buildingId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
