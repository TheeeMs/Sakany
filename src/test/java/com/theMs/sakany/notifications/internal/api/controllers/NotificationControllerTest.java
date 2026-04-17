package com.theMs.sakany.notifications.internal.api.controllers;

import com.theMs.sakany.notifications.internal.api.dtos.SendNotificationRequest;
import com.theMs.sakany.notifications.internal.application.commands.MarkAllNotificationsReadCommand;
import com.theMs.sakany.notifications.internal.application.commands.MarkAllNotificationsReadCommandHandler;
import com.theMs.sakany.notifications.internal.application.commands.MarkNotificationReadCommand;
import com.theMs.sakany.notifications.internal.application.commands.MarkNotificationReadCommandHandler;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommand;
import com.theMs.sakany.notifications.internal.application.commands.SendNotificationCommandHandler;
import com.theMs.sakany.notifications.internal.application.queries.ListNotificationsQuery;
import com.theMs.sakany.notifications.internal.application.queries.ListNotificationsQueryHandler;
import com.theMs.sakany.notifications.internal.domain.NotificationType;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private ListNotificationsQueryHandler listNotificationsQueryHandler;

    @Mock
    private MarkNotificationReadCommandHandler markNotificationReadCommandHandler;

    @Mock
    private MarkAllNotificationsReadCommandHandler markAllNotificationsReadCommandHandler;

    @Mock
    private SendNotificationCommandHandler sendNotificationCommandHandler;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(
                listNotificationsQueryHandler,
                markNotificationReadCommandHandler,
                markAllNotificationsReadCommandHandler,
                sendNotificationCommandHandler
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listNotifications_shouldRejectMismatchedRecipientId() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        assertThrows(BusinessRuleException.class, () -> controller.listNotifications(UUID.randomUUID(), null));
    }

    @Test
    void listNotifications_shouldUseAuthenticatedRecipientWhenMissing() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);
        when(listNotificationsQueryHandler.handle(any(ListNotificationsQuery.class))).thenReturn(List.of());

        controller.listNotifications(null, null);

        ArgumentCaptor<ListNotificationsQuery> captor = ArgumentCaptor.forClass(ListNotificationsQuery.class);
        verify(listNotificationsQueryHandler).handle(captor.capture());
        assertEquals(actorId, captor.getValue().recipientId());
    }

    @Test
    void markAsRead_shouldRejectMismatchedRecipientId() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        assertThrows(BusinessRuleException.class, () -> controller.markAsRead(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void markAsRead_shouldUseAuthenticatedRecipientWhenMissing() {
        UUID actorId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        setAuthentication(actorId);

        controller.markAsRead(notificationId, null);

        ArgumentCaptor<MarkNotificationReadCommand> captor = ArgumentCaptor.forClass(MarkNotificationReadCommand.class);
        verify(markNotificationReadCommandHandler).handle(captor.capture());
        assertEquals(notificationId, captor.getValue().notificationId());
        assertEquals(actorId, captor.getValue().recipientId());
    }

    @Test
    void markAllAsRead_shouldUseAuthenticatedRecipientWhenMissing() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        controller.markAllAsRead(null);

        ArgumentCaptor<MarkAllNotificationsReadCommand> captor = ArgumentCaptor.forClass(MarkAllNotificationsReadCommand.class);
        verify(markAllNotificationsReadCommandHandler).handle(captor.capture());
        assertEquals(actorId, captor.getValue().recipientId());
    }

    @Test
    void sendNotification_shouldRejectMismatchedRecipientId() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        SendNotificationRequest request = new SendNotificationRequest(
                UUID.randomUUID(),
                "title",
                "body",
                NotificationType.GENERAL,
                UUID.randomUUID(),
                null
        );

        assertThrows(BusinessRuleException.class, () -> controller.sendNotification(request));
    }

    @Test
    void sendNotification_shouldUseAuthenticatedRecipientWhenMissing() {
        UUID actorId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        setAuthentication(actorId);
        when(sendNotificationCommandHandler.handle(any(SendNotificationCommand.class))).thenReturn(notificationId);

        SendNotificationRequest request = new SendNotificationRequest(
                null,
                "title",
                "body",
                NotificationType.GENERAL,
                UUID.randomUUID(),
                null
        );

        controller.sendNotification(request);

        ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
        verify(sendNotificationCommandHandler).handle(captor.capture());
        assertEquals(actorId, captor.getValue().recipientId());
    }

    private void setAuthentication(UUID principalId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principalId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_RESIDENT"))
                )
        );
    }
}