package com.theMs.sakany.events.internal.api.controllers;

import com.theMs.sakany.events.internal.application.commands.*;
import com.theMs.sakany.events.internal.application.queries.EventDto;
import com.theMs.sakany.events.internal.application.queries.GetEventDetailsQuery;
import com.theMs.sakany.events.internal.application.queries.GetEventDetailsQueryHandler;
import com.theMs.sakany.events.internal.application.queries.ListEventsQuery;
import com.theMs.sakany.events.internal.application.queries.ListEventsQueryHandler;
import com.theMs.sakany.events.internal.domain.EventStatus;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/events")
public class EventController {

    private final ProposeEventCommandHandler proposeEventCommandHandler;
    private final ApproveEventCommandHandler approveEventCommandHandler;
    private final RejectEventCommandHandler rejectEventCommandHandler;
    private final RegisterForEventCommandHandler registerForEventCommandHandler;
    private final CancelRegistrationCommandHandler cancelRegistrationCommandHandler;
    private final GetEventDetailsQueryHandler getEventDetailsQueryHandler;
    private final ListEventsQueryHandler listEventsQueryHandler;

    public EventController(
            ProposeEventCommandHandler proposeEventCommandHandler,
            ApproveEventCommandHandler approveEventCommandHandler,
            RejectEventCommandHandler rejectEventCommandHandler,
            RegisterForEventCommandHandler registerForEventCommandHandler,
            CancelRegistrationCommandHandler cancelRegistrationCommandHandler,
            GetEventDetailsQueryHandler getEventDetailsQueryHandler,
            ListEventsQueryHandler listEventsQueryHandler) {
        this.proposeEventCommandHandler = proposeEventCommandHandler;
        this.approveEventCommandHandler = approveEventCommandHandler;
        this.rejectEventCommandHandler = rejectEventCommandHandler;
        this.registerForEventCommandHandler = registerForEventCommandHandler;
        this.cancelRegistrationCommandHandler = cancelRegistrationCommandHandler;
        this.getEventDetailsQueryHandler = getEventDetailsQueryHandler;
        this.listEventsQueryHandler = listEventsQueryHandler;
    }

    @PostMapping
    public ResponseEntity<Void> proposeEvent(@RequestBody ProposeEventCommand command) {
        UUID actorId = getAuthenticatedUserId();
        if (command.organizerId() != null && !actorId.equals(command.organizerId())) {
            throw new BusinessRuleException("organizerId must match authenticated user");
        }

        ProposeEventCommand securedCommand = new ProposeEventCommand(
                actorId,
                command.title(),
                command.description(),
                command.location(),
                command.startDate(),
                command.endDate(),
                command.imageUrl(),
                command.hostName(),
                command.price(),
                command.maxAttendees(),
                command.category(),
                command.hostRole(),
                command.contactPhone(),
                command.latitude(),
                command.longitude(),
                command.tags(),
                command.recurringEvent()
        );

        UUID eventId = proposeEventCommandHandler.handle(securedCommand);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(eventId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> listEvents(@RequestParam(required = false) EventStatus status) {
        List<EventDto> events = listEventsQueryHandler.handle(new ListEventsQuery(status));
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventDetails(@PathVariable UUID id) {
        EventDto event = getEventDetailsQueryHandler.handle(new GetEventDetailsQuery(id));
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveEvent(@PathVariable UUID id, @RequestParam(required = false) UUID adminId) {
        UUID actorId = getAuthenticatedUserId();
        if (!isAdminActor()) {
            throw new BusinessRuleException("Only admins can approve events");
        }
        if (adminId != null && !adminId.equals(actorId)) {
            throw new BusinessRuleException("adminId must match authenticated admin");
        }

        approveEventCommandHandler.handle(new ApproveEventCommand(id, actorId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectEvent(@PathVariable UUID id, @RequestParam(required = false) UUID adminId) {
        UUID actorId = getAuthenticatedUserId();
        if (!isAdminActor()) {
            throw new BusinessRuleException("Only admins can reject events");
        }
        if (adminId != null && !adminId.equals(actorId)) {
            throw new BusinessRuleException("adminId must match authenticated admin");
        }

        rejectEventCommandHandler.handle(new RejectEventCommand(id, actorId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Void> registerForEvent(@PathVariable UUID id, @RequestParam(required = false) UUID residentId) {
        UUID actorId = getAuthenticatedUserId();
        if (residentId != null && !residentId.equals(actorId)) {
            throw new BusinessRuleException("residentId must match authenticated user");
        }

        registerForEventCommandHandler.handle(new RegisterForEventCommand(id, actorId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<Void> cancelRegistration(@PathVariable UUID id, @RequestParam(required = false) UUID residentId) {
        UUID actorId = getAuthenticatedUserId();
        if (residentId != null && !residentId.equals(actorId)) {
            throw new BusinessRuleException("residentId must match authenticated user");
        }

        cancelRegistrationCommandHandler.handle(new CancelRegistrationCommand(id, actorId));
        return ResponseEntity.noContent().build();
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

    private boolean isAdminActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }
}
