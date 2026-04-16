package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import com.theMs.sakany.events.internal.domain.CommunityEventRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CommunityEventRepositoryImpl implements CommunityEventRepository {

    private final CommunityEventJpaRepository jpaRepository;
    private final CommunityEventMapper mapper;

    public CommunityEventRepositoryImpl(CommunityEventJpaRepository jpaRepository, CommunityEventMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CommunityEvent save(CommunityEvent event) {
        CommunityEventEntity entity = event.getId() == null
            ? new CommunityEventEntity()
            : jpaRepository.findById(event.getId()).orElseGet(CommunityEventEntity::new);

        entity.setId(event.getId());
        entity.setOrganizerId(event.getOrganizerId());
        entity.setTitle(event.getTitle());
        entity.setDescription(event.getDescription());
        entity.setLocation(event.getLocation());
        entity.setStartDate(event.getStartDate());
        entity.setEndDate(event.getEndDate());
        entity.setImageUrl(event.getImageUrl());
        entity.setHostName(event.getHostName());
        entity.setPrice(event.getPrice());
        entity.setMaxAttendees(event.getMaxAttendees());
        entity.setCategory(event.getCategory());
        entity.setHostRole(event.getHostRole());
        entity.setContactPhone(event.getContactPhone());
        entity.setLatitude(event.getLatitude());
        entity.setLongitude(event.getLongitude());
        entity.setCurrentAttendees(event.getCurrentAttendees());
        entity.setStatus(event.getStatus());
        entity.setApprovedBy(event.getApprovedBy());

        CommunityEventEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CommunityEvent> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
