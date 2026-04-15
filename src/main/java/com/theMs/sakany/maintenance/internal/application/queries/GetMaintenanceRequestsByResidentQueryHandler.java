package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetMaintenanceRequestsByResidentQueryHandler implements QueryHandler<GetMaintenanceRequestsByResidentQuery, List<MaintenanceRequest>> {

    private final MaintenanceRequestRepository repository;

    public GetMaintenanceRequestsByResidentQueryHandler(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MaintenanceRequest> handle(GetMaintenanceRequestsByResidentQuery query) {
        return repository.findByResidentId(query.residentId());
    }
}
