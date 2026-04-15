package com.theMs.sakany.property.internal.application.queries;

import com.theMs.sakany.property.internal.domain.Unit;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record GetUnitsByBuildingQuery(
    UUID buildingId
) implements Query<List<Unit>> {
}
