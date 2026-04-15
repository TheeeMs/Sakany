package com.theMs.sakany.notifications.shared;

import java.util.UUID;

public interface INotificationAPI {
    UUID sendNotification(
            UUID recipientId,
            String title,
            String body,
            String type,
            UUID referenceId,
            String channel
    );
}
