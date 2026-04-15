package com.theMs.sakany.notifications.internal.application;

import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommand;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommandHandler;
import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
import com.theMs.sakany.notifications.shared.INotificationAPI;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationAPIImpl implements INotificationAPI {

    private final SendNotificationCommandHandler sendNotificationCommandHandler;

    public NotificationAPIImpl(SendNotificationCommandHandler sendNotificationCommandHandler) {
        this.sendNotificationCommandHandler = sendNotificationCommandHandler;
    }

    @Override
    public UUID sendNotification(
            UUID recipientId,
            String title,
            String body,
            String type,
            UUID referenceId,
            String channel
    ) {
        NotificationType notificationType = NotificationType.valueOf(type);
        NotificationChannel notificationChannel = (channel == null || channel.isBlank())
                ? NotificationChannel.PUSH
                : NotificationChannel.valueOf(channel);

        return sendNotificationCommandHandler.handle(new SendNotificationCommand(
                recipientId,
                title,
                body,
                notificationType,
                referenceId,
                notificationChannel
        ));
    }
}
