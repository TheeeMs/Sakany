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
        CommunityEventEntity entity = mapper.toEntity(event);
        CommunityEventEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CommunityEvent> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
