package com.theMs.sakany.events.internal.api.controllers;

import com.theMs.sakany.events.internal.application.commands.*;
import com.theMs.sakany.events.internal.application.queries.EventDto;
import com.theMs.sakany.events.internal.application.queries.GetEventDetailsQuery;
import com.theMs.sakany.events.internal.application.queries.GetEventDetailsQueryHandler;
import com.theMs.sakany.events.internal.application.queries.ListEventsQuery;
import com.theMs.sakany.events.internal.application.queries.ListEventsQueryHandler;
import com.theMs.sakany.events.internal.domain.EventStatus;
import org.springframework.http.ResponseEntity;
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
        UUID eventId = proposeEventCommandHandler.handle(command);
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

    // Pass the admin ID as a request param for simulation purposes, or a header
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveEvent(@PathVariable UUID id, @RequestParam UUID adminId) {
        approveEventCommandHandler.handle(new ApproveEventCommand(id, adminId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectEvent(@PathVariable UUID id, @RequestParam UUID adminId) {
        rejectEventCommandHandler.handle(new RejectEventCommand(id, adminId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Void> registerForEvent(@PathVariable UUID id, @RequestParam UUID residentId) {
        registerForEventCommandHandler.handle(new RegisterForEventCommand(id, residentId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/register")
    public ResponseEntity<Void> cancelRegistration(@PathVariable UUID id, @RequestParam UUID residentId) {
        cancelRegistrationCommandHandler.handle(new CancelRegistrationCommand(id, residentId));
        return ResponseEntity.noContent().build();
    }
}
