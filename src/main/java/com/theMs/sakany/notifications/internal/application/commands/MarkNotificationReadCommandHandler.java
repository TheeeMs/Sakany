package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationLogRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkNotificationReadCommandHandler implements CommandHandler<MarkNotificationReadCommand, Void> {

    private final NotificationLogRepository notificationLogRepository;

    public MarkNotificationReadCommandHandler(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    @Transactional
    public Void handle(MarkNotificationReadCommand command) {
        NotificationLog notificationLog = notificationLogRepository.findById(command.notificationId())
                .orElseThrow(() -> new NotFoundException("NotificationLog", command.notificationId()));

        if (!notificationLog.getRecipientId().equals(command.recipientId())) {
            throw new BusinessRuleException("Notification does not belong to this recipient");
        }

        notificationLog.markRead();
        notificationLogRepository.save(notificationLog);
        return null;
    }
}
