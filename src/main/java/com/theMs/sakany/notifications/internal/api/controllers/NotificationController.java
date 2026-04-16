package com.theMs.sakany.notifications.internal.api.controllers;

import com.theMs.sakany.notifications.internal.application.commands.MarkAllNotificationsReadCommand;
import com.theMs.sakany.notifications.internal.application.commands.MarkAllNotificationsReadCommandHandler;
import com.theMs.sakany.notifications.internal.api.dtos.NotificationResponse;
import com.theMs.sakany.notifications.internal.api.dtos.SendNotificationRequest;
import com.theMs.sakany.notifications.internal.application.commands.MarkNotificationReadCommand;
import com.theMs.sakany.notifications.internal.application.commands.MarkNotificationReadCommandHandler;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommand;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommandHandler;
import com.theMs.sakany.notifications.internal.application.queries.ListNotificationsQuery;
import com.theMs.sakany.notifications.internal.application.queries.ListNotificationsQueryHandler;
import com.theMs.sakany.notifications.internal.domain.NotificationLog;
import com.theMs.sakany.notifications.internal.domain.NotificationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final ListNotificationsQueryHandler listNotificationsQueryHandler;
    private final MarkNotificationReadCommandHandler markNotificationReadCommandHandler;
    private final MarkAllNotificationsReadCommandHandler markAllNotificationsReadCommandHandler;
    private final SendNotificationCommandHandler sendNotificationCommandHandler;

    public NotificationController(
            ListNotificationsQueryHandler listNotificationsQueryHandler,
            MarkNotificationReadCommandHandler markNotificationReadCommandHandler,
            MarkAllNotificationsReadCommandHandler markAllNotificationsReadCommandHandler,
            SendNotificationCommandHandler sendNotificationCommandHandler
    ) {
        this.listNotificationsQueryHandler = listNotificationsQueryHandler;
        this.markNotificationReadCommandHandler = markNotificationReadCommandHandler;
        this.markAllNotificationsReadCommandHandler = markAllNotificationsReadCommandHandler;
        this.sendNotificationCommandHandler = sendNotificationCommandHandler;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listNotifications(
            @RequestParam UUID recipientId,
            @RequestParam(required = false) NotificationStatus status
    ) {
        List<NotificationResponse> response = listNotificationsQueryHandler.handle(new ListNotificationsQuery(recipientId, status))
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, @RequestParam UUID recipientId) {
        markNotificationReadCommandHandler.handle(new MarkNotificationReadCommand(id, recipientId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam UUID recipientId) {
        markAllNotificationsReadCommandHandler.handle(new MarkAllNotificationsReadCommand(recipientId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send")
    public ResponseEntity<UUID> sendNotification(@RequestBody SendNotificationRequest request) {
        UUID id = sendNotificationCommandHandler.handle(new SendNotificationCommand(
                request.recipientId(),
                request.title(),
                request.body(),
                request.type(),
                request.referenceId(),
                request.channel()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    private NotificationResponse toResponse(NotificationLog notificationLog) {
        boolean isUrgent = switch (notificationLog.getType()) {
            case ALERT, ANNOUNCEMENT, PAYMENT_DUE, MAINTENANCE_UPDATE -> true;
            default -> false;
        };

        return new NotificationResponse(
                notificationLog.getId(),
                notificationLog.getRecipientId(),
                notificationLog.getTitle(),
                notificationLog.getBody(),
                notificationLog.getType(),
                notificationLog.getReferenceId(),
                notificationLog.getChannel(),
                notificationLog.getStatus(),
                notificationLog.getSentAt(),
                notificationLog.getReadAt(),
                notificationLog.getFailureReason(),
                isUrgent
        );
    }
}
