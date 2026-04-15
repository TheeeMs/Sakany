package com.theMs.sakany.notifications.internal.application.queries;

import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationLogRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListNotificationsQueryHandler implements QueryHandler<ListNotificationsQuery, List<NotificationLog>> {

    private final NotificationLogRepository notificationLogRepository;

    public ListNotificationsQueryHandler(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    public List<NotificationLog> handle(ListNotificationsQuery query) {
        if (query.status() != null) {
            return notificationLogRepository.findByRecipientIdAndStatus(query.recipientId(), query.status());
        }
        return notificationLogRepository.findByRecipientId(query.recipientId());
    }
}
