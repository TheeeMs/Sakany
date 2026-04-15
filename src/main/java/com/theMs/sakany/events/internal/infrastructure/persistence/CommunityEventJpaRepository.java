package com.theMs.sakany.events.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import com.theMs.sakany.events.internal.domain.EventStatus;
import java.util.List;

public interface CommunityEventJpaRepository extends JpaRepository<CommunityEventEntity, UUID> {
    List<CommunityEventEntity> findByStatus(EventStatus status);
}
