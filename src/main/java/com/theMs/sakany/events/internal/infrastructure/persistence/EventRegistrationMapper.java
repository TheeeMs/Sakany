package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.EventRegistration;
import org.springframework.stereotype.Component;

@Component
public class EventRegistrationMapper {
    public EventRegistration toDomain(EventRegistrationEntity entity) {
        if (entity == null) return null;

        return new EventRegistration(
            entity.getId(),
            entity.getEventId(),
            entity.getResidentId(),
            entity.getRegisteredAt(),
            entity.getStatus()
        );
    }

    public EventRegistrationEntity toEntity(EventRegistration domain) {
        if (domain == null) return null;
        return new EventRegistrationEntity(
            domain.getId(),
            domain.getEventId(),
            domain.getResidentId(),
            domain.getRegisteredAt(),
            domain.getStatus()
        );
    }
}
