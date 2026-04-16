package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateMaintenancePriorityCommandHandler implements CommandHandler<UpdateMaintenancePriorityCommand, Void> {

    private final MaintenanceRequestRepository repository;

    public UpdateMaintenancePriorityCommandHandler(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void handle(UpdateMaintenancePriorityCommand command) {
        MaintenanceRequest request = repository.findById(command.requestId())
                .orElseThrow(() -> new NotFoundException("MaintenanceRequest", command.requestId()));

        request.updatePriority(command.priority());
        repository.save(request);
        return null;
    }
}
