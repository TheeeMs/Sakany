package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationJpaRepository extends JpaRepository<EventRegistrationEntity, UUID> {
    Optional<EventRegistrationEntity> findByEventIdAndResidentId(UUID eventId, UUID residentId);
    boolean existsByEventIdAndResidentIdAndStatus(UUID eventId, UUID residentId, RegistrationStatus status);
}
