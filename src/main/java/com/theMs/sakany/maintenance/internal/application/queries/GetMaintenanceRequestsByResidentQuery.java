package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record GetMaintenanceRequestsByResidentQuery(
        UUID residentId
) implements Query<List<MaintenanceRequest>> {
}
