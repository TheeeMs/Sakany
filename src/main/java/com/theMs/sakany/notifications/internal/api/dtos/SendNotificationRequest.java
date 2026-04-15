package com.theMs.sakany.notifications.internal.api.dtos;

import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationType;

import java.util.UUID;

public record SendNotificationRequest(
        UUID recipientId,
        String title,
        String body,
        NotificationType type,
        UUID referenceId,
        NotificationChannel channel
) {
}
