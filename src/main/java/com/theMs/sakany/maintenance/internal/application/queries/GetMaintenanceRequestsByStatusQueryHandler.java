package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequestRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetMaintenanceRequestsByStatusQueryHandler implements QueryHandler<GetMaintenanceRequestsByStatusQuery, List<MaintenanceRequest>> {

    private final MaintenanceRequestRepository repository;

    public GetMaintenanceRequestsByStatusQueryHandler(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MaintenanceRequest> handle(GetMaintenanceRequestsByStatusQuery query) {
        return repository.findByStatus(query.status());
    }
}
