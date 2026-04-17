package com.theMs.sakany.notifications.internal.api.controllers;

import com.theMs.sakany.notifications.internal.application.queries.AdminCommunicationsCenterService;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCommunicationsControllerTest {

    @Mock
    private AdminCommunicationsCenterService adminCommunicationsCenterService;

    private AdminCommunicationsController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminCommunicationsController(adminCommunicationsCenterService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPushNotification_shouldRejectMismatchedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        AdminCommunicationsController.AdminCreatePushNotificationRequest request =
                new AdminCommunicationsController.AdminCreatePushNotificationRequest(
                        UUID.randomUUID(),
                        List.of(UUID.randomUUID()),
                        false,
                        "title",
                        "message",
                        "NORMAL",
                        null
                );

        assertThrows(BusinessRuleException.class, () -> controller.createPushNotification(request));
    }

    @Test
    void createPushNotification_shouldUseAuthenticatedAdminWhenMissing() {
        UUID authenticatedAdminId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        AdminCommunicationsController.AdminCreatePushNotificationRequest request =
                new AdminCommunicationsController.AdminCreatePushNotificationRequest(
                        null,
                        List.of(UUID.randomUUID()),
                        false,
                        "title",
                        "message",
                        "NORMAL",
                        null
                );

        when(adminCommunicationsCenterService.createPushNotification(
                authenticatedAdminId,
                request.recipientIds(),
                request.sendToAll(),
                request.title(),
                request.message(),
                request.priority(),
                request.scheduleAt()
        )).thenReturn(new AdminCommunicationsCenterService.CreatePushNotificationResult(
                campaignId,
                1,
                "SENT",
                "Admin"
        ));

        controller.createPushNotification(request);

        verify(adminCommunicationsCenterService).createPushNotification(
                authenticatedAdminId,
                request.recipientIds(),
                request.sendToAll(),
                request.title(),
                request.message(),
                request.priority(),
                request.scheduleAt()
        );
    }

    @Test
    void createAnnouncement_shouldRejectMismatchedAuthorId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        AdminCommunicationsController.AdminCreateAnnouncementRequest request =
                new AdminCommunicationsController.AdminCreateAnnouncementRequest(
                        UUID.randomUUID(),
                        "title",
                        "content",
                        "NORMAL",
                        null
                );

        assertThrows(BusinessRuleException.class, () -> controller.createAnnouncement(request));
    }

    @Test
    void createAnnouncement_shouldUseAuthenticatedAdminWhenMissing() {
        UUID authenticatedAdminId = UUID.randomUUID();
        UUID announcementId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        when(adminCommunicationsCenterService.createAnnouncement(
                authenticatedAdminId,
                "title",
                "content",
                "NORMAL",
                null
        )).thenReturn(announcementId);

        AdminCommunicationsController.AdminCreateAnnouncementRequest request =
                new AdminCommunicationsController.AdminCreateAnnouncementRequest(
                        null,
                        "title",
                        "content",
                        "NORMAL",
                        null
                );

        UUID result = controller.createAnnouncement(request).getBody();
        assertEquals(announcementId, result);

        verify(adminCommunicationsCenterService).createAnnouncement(
                authenticatedAdminId,
                request.title(),
                request.content(),
                request.priority(),
                request.expiresAt()
        );
    }

    private void setAdminAuthentication(UUID principalId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principalId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }
}