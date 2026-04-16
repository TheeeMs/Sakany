package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import com.theMs.sakany.events.internal.domain.CommunityEventRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteEventCommandHandler implements CommandHandler<CompleteEventCommand, Void> {

    private final CommunityEventRepository eventRepository;

    public CompleteEventCommandHandler(CommunityEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Void handle(CompleteEventCommand command) {
        CommunityEvent event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new NotFoundException("CommunityEvent", command.eventId().toString()));

        event.complete();
        eventRepository.save(event);
        return null;
    }
}
