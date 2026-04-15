package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.EventRegistration;
import com.theMs.sakany.events.internal.domain.EventRegistrationRepository;
import com.theMs.sakany.events.internal.domain.RegistrationStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class EventRegistrationRepositoryImpl implements EventRegistrationRepository {

    private final EventRegistrationJpaRepository jpaRepository;
    private final EventRegistrationMapper mapper;

    public EventRegistrationRepositoryImpl(EventRegistrationJpaRepository jpaRepository, EventRegistrationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public EventRegistration save(EventRegistration registration) {
        EventRegistrationEntity entity = mapper.toEntity(registration);
        EventRegistrationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EventRegistration> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<EventRegistration> findByEventIdAndResidentId(UUID eventId, UUID residentId) {
        return jpaRepository.findByEventIdAndResidentId(eventId, residentId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEventIdAndResidentIdAndStatus(UUID eventId, UUID residentId, RegistrationStatus status) {
        return jpaRepository.existsByEventIdAndResidentIdAndStatus(eventId, residentId, status);
    }
}
