package com.theMs.sakany.maintenance.internal.application.commands;

import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.TechnicianProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignTechnicianCommandHandler implements CommandHandler<AssignTechnicianCommand, Void> {

    private final MaintenanceRequestRepository repository;
    private final UserJpaRepository userJpaRepository;
    private final TechnicianProfileJpaRepository technicianProfileJpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AssignTechnicianCommandHandler(
            MaintenanceRequestRepository repository,
            UserJpaRepository userJpaRepository,
            TechnicianProfileJpaRepository technicianProfileJpaRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.userJpaRepository = userJpaRepository;
        this.technicianProfileJpaRepository = technicianProfileJpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(AssignTechnicianCommand command) {
        MaintenanceRequest request = repository.findById(command.requestId())
                .orElseThrow(() -> new NotFoundException("MaintenanceRequest", command.requestId()));

        UserEntity technician = userJpaRepository.findById(command.technicianId())
                .orElseThrow(() -> new NotFoundException("Technician", command.technicianId()));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BusinessRuleException("Assigned user must have TECHNICIAN role");
        }

        if (!technician.isActive()) {
            throw new BusinessRuleException("Assigned technician account is inactive");
        }

        if (technicianProfileJpaRepository.findByUserId(technician.getId()).isEmpty()) {
            throw new BusinessRuleException("Technician profile not found");
        }

        request.assign(command.technicianId());
        
        repository.save(request);

        request.getDomainEvents().forEach(eventPublisher::publishEvent);
        request.clearDomainEvents();

        return null;
    }
}
