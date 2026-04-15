package com.theMs.sakany.events.internal.application.queries;

import com.theMs.sakany.events.internal.infrastructure.persistence.CommunityEventEntity;
import com.theMs.sakany.events.internal.infrastructure.persistence.CommunityEventJpaRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetEventDetailsQueryHandler implements QueryHandler<GetEventDetailsQuery, EventDto> {

    private final CommunityEventJpaRepository jpaRepository;

    public GetEventDetailsQueryHandler(CommunityEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EventDto handle(GetEventDetailsQuery query) {
        return jpaRepository.findById(query.eventId())
            .map(this::mapToDto)
            .orElseThrow(() -> new NotFoundException("CommunityEvent", query.eventId().toString()));
    }

    private EventDto mapToDto(CommunityEventEntity entity) {
        return new EventDto(
            entity.getId(),
            entity.getOrganizerId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getLocation(),
            entity.getEventDate(),
            entity.getMaxAttendees(),
            entity.getCurrentAttendees(),
            entity.getStatus(),
            entity.getApprovedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
