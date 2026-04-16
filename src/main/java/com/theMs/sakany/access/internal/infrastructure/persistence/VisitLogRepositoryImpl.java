package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.access.internal.domain.VisitLog;
import com.theMs.sakany.access.internal.domain.VisitLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class VisitLogRepositoryImpl implements VisitLogRepository {

    private final VisitLogJpaRepository jpaRepository;
    private final VisitLogMapper mapper;

    public VisitLogRepositoryImpl(VisitLogJpaRepository jpaRepository, VisitLogMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VisitLog save(VisitLog visitLog) {
        VisitLogEntity entity = visitLog.getId() == null
            ? new VisitLogEntity()
            : jpaRepository.findById(visitLog.getId()).orElseGet(VisitLogEntity::new);

        entity.setId(visitLog.getId());
        entity.setAccessCodeId(visitLog.getAccessCodeId());
        entity.setResidentId(visitLog.getResidentId());
        entity.setVisitorName(visitLog.getVisitorName());
        entity.setEntryTime(visitLog.getEntryTime());
        entity.setExitTime(visitLog.getExitTime());
        entity.setGateNumber(visitLog.getGateNumber());
        VisitLogEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VisitLog> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<VisitLog> findByResidentId(UUID residentId) {
        return jpaRepository.findByResidentId(residentId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<VisitLog> findByAccessCodeId(UUID accessCodeId) {
        return jpaRepository.findByAccessCodeId(accessCodeId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
