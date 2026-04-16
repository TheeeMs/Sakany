package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRegistrationJpaRepository extends JpaRepository<EventRegistrationEntity, UUID> {
    Optional<EventRegistrationEntity> findByEventIdAndResidentId(UUID eventId, UUID residentId);
    boolean existsByEventIdAndResidentIdAndStatus(UUID eventId, UUID residentId, RegistrationStatus status);

    @Query(value = """
            SELECT
                er.resident_id AS residentId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.phone AS phoneNumber,
                u.email AS email,
                er.registered_at AS registeredAt
            FROM event_registrations er
            JOIN users u ON u.id = er.resident_id
            WHERE er.event_id = :eventId
              AND er.status = 'REGISTERED'
            ORDER BY er.registered_at ASC
            """,
            nativeQuery = true)
    List<EventAttendeeRow> findRegisteredAttendees(@Param("eventId") UUID eventId);
}
