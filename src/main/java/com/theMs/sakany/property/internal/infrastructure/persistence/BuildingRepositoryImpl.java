package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.Building;
import com.theMs.sakany.property.internal.domain.BuildingRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class BuildingRepositoryImpl implements BuildingRepository {
    private final BuildingJpaRepository jpaRepository;
    private final BuildingMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public BuildingRepositoryImpl(BuildingJpaRepository jpaRepository, BuildingMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Building save(Building building) {
        BuildingEntity entity = mapper.toEntity(building);
        BuildingEntity savedEntity = jpaRepository.save(entity);

        building.getDomainEvents().forEach(eventPublisher::publishEvent);
        building.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Building> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Building> findByCompoundId(UUID compoundId) {
        return jpaRepository.findByCompoundId(compoundId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
