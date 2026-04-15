package com.theMs.sakany.notifications.internal.application.queries;

import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record ListNotificationsQuery(
        UUID recipientId,
        NotificationStatus status
) implements Query<List<NotificationLog>> {
}
