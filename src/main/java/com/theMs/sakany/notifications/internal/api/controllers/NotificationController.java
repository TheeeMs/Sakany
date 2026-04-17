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
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @RequestParam(required = false) UUID recipientId,
            @RequestParam(required = false) NotificationStatus status
    ) {
        UUID actorId = resolveRecipientFromAuthenticatedUser(recipientId);

        List<NotificationResponse> response = listNotificationsQueryHandler.handle(new ListNotificationsQuery(actorId, status))
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, @RequestParam(required = false) UUID recipientId) {
        UUID actorId = resolveRecipientFromAuthenticatedUser(recipientId);
        markNotificationReadCommandHandler.handle(new MarkNotificationReadCommand(id, actorId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam(required = false) UUID recipientId) {
        UUID actorId = resolveRecipientFromAuthenticatedUser(recipientId);
        markAllNotificationsReadCommandHandler.handle(new MarkAllNotificationsReadCommand(actorId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send")
    public ResponseEntity<UUID> sendNotification(@RequestBody SendNotificationRequest request) {
        UUID actorId = resolveRecipientFromAuthenticatedUser(request.recipientId());

        UUID id = sendNotificationCommandHandler.handle(new SendNotificationCommand(
                actorId,
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

    private UUID resolveRecipientFromAuthenticatedUser(UUID requestedRecipientId) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        if (requestedRecipientId != null && !requestedRecipientId.equals(authenticatedUserId)) {
            throw new BusinessRuleException("recipientId must match authenticated user");
        }

        return authenticatedUserId;
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }

        try {
            return UUID.fromString(principal.toString());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid authenticated principal");
        }
    }
}
