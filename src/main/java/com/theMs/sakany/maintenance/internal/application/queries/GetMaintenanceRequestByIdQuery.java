package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.UUID;

public record GetMaintenanceRequestByIdQuery(
        UUID id
) implements Query<MaintenanceRequest> {
}
