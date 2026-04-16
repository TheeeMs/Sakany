package com.theMs.sakany.notifications.internal.api.dtos;

import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import com.theMs.sakany.notifications.internal.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        String title,
        String body,
        NotificationType type,
        UUID referenceId,
        NotificationChannel channel,
        NotificationStatus status,
        Instant sentAt,
        Instant readAt,
        String failureReason,
        boolean isUrgent
) {
}
