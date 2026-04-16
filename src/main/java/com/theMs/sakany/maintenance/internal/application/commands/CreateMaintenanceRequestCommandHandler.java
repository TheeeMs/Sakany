package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateMaintenanceRequestCommandHandler implements CommandHandler<CreateMaintenanceRequestCommand, UUID> {

    private final MaintenanceRequestRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateMaintenanceRequestCommandHandler(MaintenanceRequestRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UUID handle(CreateMaintenanceRequestCommand command) {
        MaintenanceRequest request = MaintenanceRequest.create(
                command.residentId(),
                command.unitId(),
                command.title(),
                command.description(),
            command.locationLabel(),
                command.category(),
                command.priority(),
                command.isPublic(),
                command.photoUrls()
        );

        MaintenanceRequest savedRequest = repository.save(request);
        
        request.getDomainEvents().forEach(eventPublisher::publishEvent);
        request.clearDomainEvents();

        return savedRequest.getId();
    }
}
