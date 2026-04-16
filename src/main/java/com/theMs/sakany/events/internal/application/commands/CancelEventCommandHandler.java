package com.theMs.sakany.events.internal.application.commands;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import com.theMs.sakany.events.internal.domain.CommunityEventRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelEventCommandHandler implements CommandHandler<CancelEventCommand, Void> {

    private final CommunityEventRepository eventRepository;

    public CancelEventCommandHandler(CommunityEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Void handle(CancelEventCommand command) {
        CommunityEvent event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new NotFoundException("CommunityEvent", command.eventId().toString()));

        event.cancel();
        eventRepository.save(event);
        return null;
    }
}
