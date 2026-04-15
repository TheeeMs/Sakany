package com.theMs.sakany.notifications.internal.domain;

import com.theMs.sakany.notifications.internal.domain.events.NotificationFailed;
import com.theMs.sakany.notifications.internal.domain.events.NotificationRead;
import com.theMs.sakany.notifications.internal.domain.events.NotificationSent;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class NotificationLog extends AggregateRoot {
    private UUID id;
    private UUID recipientId;
    private String title;
    private String body;
    private NotificationType type;
    private UUID referenceId;
    private NotificationChannel channel;
    private NotificationStatus status;
    private Instant sentAt;
    private Instant readAt;
    private String failureReason;

    private NotificationLog(
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
            String failureReason
    ) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.body = body;
        this.type = type;
        this.referenceId = referenceId;
        this.channel = channel;
        this.status = status;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.failureReason = failureReason;
    }

    public static NotificationLog create(
            UUID recipientId,
            String title,
            String body,
            NotificationType type,
            UUID referenceId,
            NotificationChannel channel
    ) {
        if (recipientId == null) {
            throw new BusinessRuleException("Notification recipientId cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessRuleException("Notification title cannot be null or empty");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new BusinessRuleException("Notification body cannot be null or empty");
        }
        if (type == null) {
            throw new BusinessRuleException("Notification type cannot be null");
        }
        if (channel == null) {
            throw new BusinessRuleException("Notification channel cannot be null");
        }

        return new NotificationLog(
                UUID.randomUUID(),
                recipientId,
                title,
                body,
                type,
                referenceId,
                channel,
                NotificationStatus.PENDING,
                null,
                null,
                null
        );
    }

    public void markSent() {
        if (status == NotificationStatus.SENT || status == NotificationStatus.READ) {
            throw new BusinessRuleException("Notification is already sent");
        }
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
        registerEvent(new NotificationSent(id, recipientId, sentAt));
    }

    public void markRead() {
        if (status == NotificationStatus.READ) {
            return;
        }
        if (status != NotificationStatus.SENT) {
            throw new BusinessRuleException("Only sent notifications can be marked as read");
        }
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
        registerEvent(new NotificationRead(id, recipientId, readAt));
    }

    public void markFailed(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Failure reason cannot be null or empty");
        }
        if (status == NotificationStatus.READ) {
            throw new BusinessRuleException("Read notifications cannot be marked as failed");
        }
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        registerEvent(new NotificationFailed(id, recipientId, reason));
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public NotificationType getType() {
        return type;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
