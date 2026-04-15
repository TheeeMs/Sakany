package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import com.theMs.sakany.events.internal.domain.CommunityEventRepository;
import com.theMs.sakany.events.internal.domain.EventRegistration;
import com.theMs.sakany.events.internal.domain.EventRegistrationRepository;
import com.theMs.sakany.events.internal.domain.RegistrationStatus;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterForEventCommandHandler implements CommandHandler<RegisterForEventCommand, UUID> {

    private final CommunityEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public RegisterForEventCommandHandler(CommunityEventRepository eventRepository, EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public UUID handle(RegisterForEventCommand command) {
        CommunityEvent event = eventRepository.findById(command.eventId())
            .orElseThrow(() -> new NotFoundException("CommunityEvent", command.eventId().toString()));

        if (registrationRepository.existsByEventIdAndResidentIdAndStatus(command.eventId(), command.residentId(), RegistrationStatus.REGISTERED)) {
            throw new BusinessRuleException("Resident is already registered for this event");
        }

        event.incrementAttendees();
        eventRepository.save(event);

        EventRegistration registration = EventRegistration.register(command.eventId(), command.residentId());
        registrationRepository.save(registration);

        return registration.getId();
    }
}
