package com.theMs.sakany.events.internal.api.controllers;

import com.theMs.sakany.events.internal.application.commands.ApproveEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.CancelRegistrationCommandHandler;
import com.theMs.sakany.events.internal.application.commands.ProposeEventCommand;
import com.theMs.sakany.events.internal.application.commands.ProposeEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.RegisterForEventCommand;
import com.theMs.sakany.events.internal.application.commands.RegisterForEventCommandHandler;
import com.theMs.sakany.events.internal.application.commands.RejectEventCommandHandler;
import com.theMs.sakany.events.internal.application.queries.GetEventDetailsQueryHandler;
import com.theMs.sakany.events.internal.application.queries.ListEventsQueryHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private ProposeEventCommandHandler proposeEventCommandHandler;

    @Mock
    private ApproveEventCommandHandler approveEventCommandHandler;

    @Mock
    private RejectEventCommandHandler rejectEventCommandHandler;

    @Mock
    private RegisterForEventCommandHandler registerForEventCommandHandler;

    @Mock
    private CancelRegistrationCommandHandler cancelRegistrationCommandHandler;

    @Mock
    private GetEventDetailsQueryHandler getEventDetailsQueryHandler;

    @Mock
    private ListEventsQueryHandler listEventsQueryHandler;

    private EventController controller;

    @BeforeEach
    void setUp() {
        controller = new EventController(
                proposeEventCommandHandler,
                approveEventCommandHandler,
                rejectEventCommandHandler,
                registerForEventCommandHandler,
                cancelRegistrationCommandHandler,
                getEventDetailsQueryHandler,
                listEventsQueryHandler
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest("POST", "/v1/events"))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void proposeEvent_shouldRejectSpoofedOrganizerId() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId, false);

        ProposeEventCommand command = validProposeCommand(UUID.randomUUID());

        assertThrows(BusinessRuleException.class, () -> controller.proposeEvent(command));
    }

    @Test
    void proposeEvent_shouldUseAuthenticatedUserAsOrganizer() {
        UUID actorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        setAuthentication(actorId, false);
        when(proposeEventCommandHandler.handle(any(ProposeEventCommand.class))).thenReturn(eventId);

        ProposeEventCommand command = validProposeCommand(null);

        ResponseEntity<Void> response = controller.proposeEvent(command);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ArgumentCaptor<ProposeEventCommand> captor = ArgumentCaptor.forClass(ProposeEventCommand.class);
        verify(proposeEventCommandHandler).handle(captor.capture());
        assertEquals(actorId, captor.getValue().organizerId());
    }

    @Test
    void approveEvent_shouldRejectNonAdminCaller() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId, false);

        assertThrows(BusinessRuleException.class, () -> controller.approveEvent(UUID.randomUUID(), null));
    }

    @Test
    void approveEvent_shouldRejectMismatchedAdminId() {
        UUID adminId = UUID.randomUUID();
        setAuthentication(adminId, true);

        assertThrows(BusinessRuleException.class, () -> controller.approveEvent(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void registerForEvent_shouldRejectMismatchedResidentId() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId, false);

        assertThrows(BusinessRuleException.class, () -> controller.registerForEvent(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void registerForEvent_shouldUseAuthenticatedResident() {
        UUID actorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        setAuthentication(actorId, false);
        when(registerForEventCommandHandler.handle(any(RegisterForEventCommand.class))).thenReturn(UUID.randomUUID());

        controller.registerForEvent(eventId, null);

        ArgumentCaptor<RegisterForEventCommand> captor = ArgumentCaptor.forClass(RegisterForEventCommand.class);
        verify(registerForEventCommandHandler).handle(captor.capture());
        assertEquals(actorId, captor.getValue().residentId());
        assertEquals(eventId, captor.getValue().eventId());
    }

    private ProposeEventCommand validProposeCommand(UUID organizerId) {
        Instant startDate = Instant.now().plusSeconds(7200);
        Instant endDate = startDate.plusSeconds(7200);
        return new ProposeEventCommand(
                organizerId,
                "Test Event",
                "Test Description",
                "Community Hall",
                startDate,
                endDate,
                null,
                "Host",
                null,
                100,
                "OTHER",
                null,
                "+201000000000",
                null,
                null,
                null,
                false
        );
    }

    private void setAuthentication(UUID principalId, boolean isAdmin) {
        List<SimpleGrantedAuthority> authorities = isAdmin
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_RESIDENT"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principalId, null, authorities)
        );
    }
}
