package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record SendNotificationCommand(
        UUID recipientId,
        String title,
        String body,
        NotificationType type,
        UUID referenceId,
        NotificationChannel channel
) implements Command<UUID> {
}
