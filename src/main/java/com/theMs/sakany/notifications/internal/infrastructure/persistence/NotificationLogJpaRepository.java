package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogJpaRepository extends JpaRepository<NotificationLogEntity, UUID> {
    List<NotificationLogEntity> findByRecipientId(UUID recipientId);

    List<NotificationLogEntity> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status);
}
