package com.theMs.sakany.events.internal.api.controllers;

import com.theMs.sakany.events.internal.application.commands.ApproveEventCommand;
import com.theMs.sakany.events.internal.application.commands.ApproveEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.CancelEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.CompleteEventCommand;
import com.theMs.sakany.events.internal.application.commands.CompleteEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.ProposeEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.RejectEventCommandHandler;
import com.theMs.sakany.events.internal.application.queries.AdminEventCardMenuService;
import com.theMs.sakany.events.internal.application.queries.AdminEventsManagerService;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminEventsControllerTest {

    @Mock
    private AdminEventsManagerService adminEventsManagerService;

    @Mock
    private AdminEventCardMenuService adminEventCardMenuService;

    @Mock
    private ProposeEventCommandHandler proposeEventCommandHandler;

    @Mock
    private ApproveEventCommandHandler approveEventCommandHandler;

    @Mock
    private RejectEventCommandHandler rejectEventCommandHandler;

    @Mock
    private CompleteEventCommandHandler completeEventCommandHandler;

    @Mock
    private CancelEventCommandHandler cancelEventCommandHandler;

    private AdminEventsController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminEventsController(
                adminEventsManagerService,
                adminEventCardMenuService,
                proposeEventCommandHandler,
                approveEventCommandHandler,
                rejectEventCommandHandler,
                completeEventCommandHandler,
                cancelEventCommandHandler
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void approveEvent_shouldRejectMismatchedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        AdminEventsController.AdminActorRequest request = new AdminEventsController.AdminActorRequest(UUID.randomUUID());

        assertThrows(BusinessRuleException.class, () -> controller.approveEvent(UUID.randomUUID(), request));
    }

    @Test
    void approveEvent_shouldUseAuthenticatedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        controller.approveEvent(eventId, null);

        ArgumentCaptor<ApproveEventCommand> captor = ArgumentCaptor.forClass(ApproveEventCommand.class);
        verify(approveEventCommandHandler).handle(captor.capture());
        assertEquals(eventId, captor.getValue().eventId());
        assertEquals(authenticatedAdminId, captor.getValue().adminId());
    }

    @Test
    void completeEvent_shouldRejectMismatchedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        AdminEventsController.AdminActorRequest request = new AdminEventsController.AdminActorRequest(UUID.randomUUID());

        assertThrows(BusinessRuleException.class, () -> controller.completeEvent(UUID.randomUUID(), request));
    }

    @Test
    void completeEvent_shouldUseAuthenticatedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        controller.completeEvent(eventId, null);

        ArgumentCaptor<CompleteEventCommand> captor = ArgumentCaptor.forClass(CompleteEventCommand.class);
        verify(completeEventCommandHandler).handle(captor.capture());
        assertEquals(eventId, captor.getValue().eventId());
        assertEquals(authenticatedAdminId, captor.getValue().adminId());
    }

    @Test
    void deleteEvent_shouldRejectMismatchedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        assertThrows(BusinessRuleException.class, () -> controller.deleteEvent(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void deleteEvent_shouldUseAuthenticatedAdminId() {
        UUID authenticatedAdminId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        setAdminAuthentication(authenticatedAdminId);

        controller.deleteEvent(eventId, null);

        verify(adminEventCardMenuService).deleteEvent(eventId, authenticatedAdminId);
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
