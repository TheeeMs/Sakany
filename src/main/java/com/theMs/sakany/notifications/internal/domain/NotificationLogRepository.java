package com.theMs.sakany.notifications.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository {
    NotificationLog save(NotificationLog notificationLog);

    Optional<NotificationLog> findById(UUID id);

    List<NotificationLog> findByRecipientId(UUID recipientId);

    List<NotificationLog> findByRecipientIdAndStatus(UUID recipientId, NotificationStatus status);
}
