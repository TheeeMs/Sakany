package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceTimelineEventEntity;
import com.theMs.sakany.maintenance.internal.infrastructure.persistence.MaintenanceTimelineEventJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MaintenanceTimelineService {

    private final MaintenanceTimelineEventJpaRepository timelineEventJpaRepository;

    public MaintenanceTimelineService(MaintenanceTimelineEventJpaRepository timelineEventJpaRepository) {
        this.timelineEventJpaRepository = timelineEventJpaRepository;
    }

    @Transactional
    public void record(UUID requestId, MaintenanceTimelineEventType eventType, String title, String details, UUID actorId) {
        MaintenanceTimelineEventEntity event = new MaintenanceTimelineEventEntity();
        event.setId(UUID.randomUUID());
        event.setRequestId(requestId);
        event.setEventType(eventType.name());
        event.setTitle(title);
        event.setDetails(details);
        event.setActorId(actorId);
        timelineEventJpaRepository.save(event);
    }
}
