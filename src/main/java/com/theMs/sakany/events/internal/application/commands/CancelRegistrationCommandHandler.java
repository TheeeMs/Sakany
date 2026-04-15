package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import com.theMs.sakany.events.internal.domain.CommunityEventRepository;
import com.theMs.sakany.events.internal.domain.EventRegistration;
import com.theMs.sakany.events.internal.domain.EventRegistrationRepository;
import com.theMs.sakany.events.internal.domain.RegistrationStatus;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelRegistrationCommandHandler implements CommandHandler<CancelRegistrationCommand, Void> {

    private final CommunityEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public CancelRegistrationCommandHandler(CommunityEventRepository eventRepository, EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public Void handle(CancelRegistrationCommand command) {
        EventRegistration registration = registrationRepository.findByEventIdAndResidentId(command.eventId(), command.residentId())
            .orElseThrow(() -> new NotFoundException("EventRegistration", command.eventId().toString() + "-" + command.residentId().toString()));

        if (registration.getStatus() == RegistrationStatus.REGISTERED) {
            registration.cancel();
            registrationRepository.save(registration);

            CommunityEvent event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new NotFoundException("CommunityEvent", command.eventId().toString()));
            event.decrementAttendees();
            eventRepository.save(event);
        }

        return null;
    }
}
