package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectCommandHandler implements CommandHandler<RejectCommand, Void> {

    private final MaintenanceRequestRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public RejectCommandHandler(MaintenanceRequestRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(RejectCommand command) {
        MaintenanceRequest request = repository.findById(command.requestId())
                .orElseThrow(() -> new NotFoundException("MaintenanceRequest", command.requestId()));

        request.reject(command.reason());
        
        repository.save(request);

        request.getDomainEvents().forEach(eventPublisher::publishEvent);
        request.clearDomainEvents();

        return null;
    }
}
