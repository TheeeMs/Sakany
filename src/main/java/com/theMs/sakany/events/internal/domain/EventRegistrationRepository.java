package com.theMs.sakany.events.internal.domain;

import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationRepository {
    EventRegistration save(EventRegistration registration);
    Optional<EventRegistration> findById(UUID id);
    Optional<EventRegistration> findByEventIdAndResidentId(UUID eventId, UUID residentId);
    boolean existsByEventIdAndResidentIdAndStatus(UUID eventId, UUID residentId, RegistrationStatus status);
}
