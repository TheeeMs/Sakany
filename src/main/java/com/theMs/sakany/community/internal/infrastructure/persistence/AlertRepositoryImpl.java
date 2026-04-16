package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AlertRepositoryImpl implements AlertRepository {

    private final AlertJpaRepository jpaRepository;
    private final AlertMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public AlertRepositoryImpl(AlertJpaRepository jpaRepository, AlertMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Alert save(Alert alert) {
        AlertEntity entity;
        if (alert.getId() != null && jpaRepository.existsById(alert.getId())) {
             entity = jpaRepository.findById(alert.getId()).orElseThrow();
             entity.setReporterId(alert.getReporterId());
             entity.setType(alert.getType());
             entity.setCategory(alert.getCategory());
             entity.setStatus(alert.getStatus());
             entity.setTitle(alert.getTitle());
             entity.setDescription(alert.getDescription());
             entity.setLocation(alert.getLocation());
             entity.setEventTime(alert.getEventTime());
             entity.setPhotoUrls(alert.getPhotoUrls());
             entity.setResolved(alert.isResolved());
             entity.setResolvedAt(alert.getResolvedAt());
        } else {
             entity = mapper.toEntity(alert);
        }
        
        AlertEntity savedEntity = jpaRepository.save(entity);

        // Publish events
        alert.getDomainEvents().forEach(eventPublisher::publishEvent);
        alert.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Alert> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Alert> findActiveAlerts() {
        return jpaRepository.findByIsResolvedFalseOrderByCreatedAtDesc().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
