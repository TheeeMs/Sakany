package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetMaintenanceRequestByIdQueryHandler implements QueryHandler<GetMaintenanceRequestByIdQuery, MaintenanceRequest> {

    private final MaintenanceRequestRepository repository;

    public GetMaintenanceRequestByIdQueryHandler(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public MaintenanceRequest handle(GetMaintenanceRequestByIdQuery query) {
        return repository.findById(query.id())
                .orElseThrow(() -> new NotFoundException("MaintenanceRequest", query.id()));
    }
}
