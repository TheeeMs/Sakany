package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.NotificationChannel;
import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationLogRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SendNotificationCommandHandler implements CommandHandler<SendNotificationCommand, UUID> {

    private final NotificationLogRepository notificationLogRepository;

    public SendNotificationCommandHandler(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    @Transactional
    public UUID handle(SendNotificationCommand command) {
        NotificationChannel channel = command.channel() == null ? NotificationChannel.PUSH : command.channel();

        NotificationLog notificationLog = NotificationLog.create(
                command.recipientId(),
                command.title(),
                command.body(),
                command.type(),
                command.referenceId(),
                channel
        );

        // FCM/APNs integration is out of scope for now; we mark as sent after persistence intent.
        notificationLog.markSent();
        NotificationLog saved = notificationLogRepository.save(notificationLog);
        return saved.getId();
    }
}
