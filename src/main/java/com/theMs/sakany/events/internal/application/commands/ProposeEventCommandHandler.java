package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import com.theMs.sakany.events.internal.domain.CommunityEventRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProposeEventCommandHandler implements CommandHandler<ProposeEventCommand, UUID> {

    private final CommunityEventRepository eventRepository;

    public ProposeEventCommandHandler(CommunityEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public UUID handle(ProposeEventCommand command) {
        CommunityEvent event = CommunityEvent.propose(
                command.organizerId(),
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
                command.longitude()
        );

        eventRepository.save(event);
        return event.getId();
    }
}
