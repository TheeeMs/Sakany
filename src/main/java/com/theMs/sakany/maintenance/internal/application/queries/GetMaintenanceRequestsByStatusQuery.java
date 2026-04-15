package com.theMs.sakany.maintenance.internal.application.queries;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceStatus;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;

public record GetMaintenanceRequestsByStatusQuery(
        MaintenanceStatus status
) implements Query<List<MaintenanceRequest>> {
}
