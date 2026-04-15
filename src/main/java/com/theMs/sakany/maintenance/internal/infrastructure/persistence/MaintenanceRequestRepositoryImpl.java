package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class MaintenanceRequestRepositoryImpl implements MaintenanceRequestRepository {

    private final MaintenanceRequestJpaRepository jpaRepository;
    private final MaintenanceRequestMapper mapper;

    public MaintenanceRequestRepositoryImpl(MaintenanceRequestJpaRepository jpaRepository, MaintenanceRequestMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MaintenanceRequest save(MaintenanceRequest request) {
        MaintenanceRequestEntity entity = mapper.toEntity(request);
        MaintenanceRequestEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<MaintenanceRequest> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MaintenanceRequest> findByResidentId(UUID residentId) {
        return jpaRepository.findByResidentId(residentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceRequest> findByStatus(MaintenanceStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
