package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationLogRepository;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarkAllNotificationsReadCommandHandler implements CommandHandler<MarkAllNotificationsReadCommand, Void> {

    private final NotificationLogRepository notificationLogRepository;

    public MarkAllNotificationsReadCommandHandler(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    @Transactional
    public Void handle(MarkAllNotificationsReadCommand command) {
        List<NotificationLog> unreadNotifications = notificationLogRepository.findByRecipientIdAndStatus(command.recipientId(), NotificationStatus.SENT);
        
        for (NotificationLog notificationLog : unreadNotifications) {
            notificationLog.markRead();
            notificationLogRepository.save(notificationLog);
        }
        
        return null;
    }
}