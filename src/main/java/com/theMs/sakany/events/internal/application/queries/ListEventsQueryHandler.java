package com.theMs.sakany.events.internal.application.queries;

import com.theMs.sakany.events.internal.domain.EventStatus;
import com.theMs.sakany.events.internal.infrastructure.persistence.CommunityEventEntity;
import com.theMs.sakany.events.internal.infrastructure.persistence.CommunityEventJpaRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListEventsQueryHandler implements QueryHandler<ListEventsQuery, List<EventDto>> {

    private final CommunityEventJpaRepository jpaRepository;

    public ListEventsQueryHandler(CommunityEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<EventDto> handle(ListEventsQuery query) {
        List<CommunityEventEntity> entities;
        if (query.status() != null) {
            entities = jpaRepository.findByStatus(query.status());
        } else {
            entities = jpaRepository.findAll();
        }

        return entities.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
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
