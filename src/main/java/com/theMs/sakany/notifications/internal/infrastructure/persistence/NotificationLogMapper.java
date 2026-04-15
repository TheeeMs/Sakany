package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

@Component
public class NotificationLogMapper {

    public NotificationLogEntity toEntity(NotificationLog notificationLog) {
        if (notificationLog == null) {
            return null;
        }

        NotificationLogEntity entity = new NotificationLogEntity(
                notificationLog.getRecipientId(),
                notificationLog.getTitle(),
                notificationLog.getBody(),
                notificationLog.getType(),
                notificationLog.getReferenceId(),
                notificationLog.getChannel(),
                notificationLog.getStatus(),
                notificationLog.getSentAt(),
                notificationLog.getReadAt(),
                notificationLog.getFailureReason()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, notificationLog.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on NotificationLogEntity", e);
        }

        return entity;
    }

    public NotificationLog toDomain(NotificationLogEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<NotificationLog> constructor = NotificationLog.class.getDeclaredConstructor(
                    UUID.class,
                    UUID.class,
                    String.class,
                    String.class,
                    NotificationType.class,
                    UUID.class,
                    NotificationChannel.class,
                    NotificationStatus.class,
                    Instant.class,
                    Instant.class,
                    String.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(),
                    entity.getRecipientId(),
                    entity.getTitle(),
                    entity.getBody(),
                    entity.getType(),
                    entity.getReferenceId(),
                    entity.getChannel(),
                    entity.getStatus(),
                    entity.getSentAt(),
                    entity.getReadAt(),
                    entity.getFailureReason()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map NotificationLogEntity to domain", e);
        }
    }
}
