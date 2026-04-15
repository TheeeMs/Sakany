package com.theMs.sakany.property.internal.application.queries;

import com.theMs.sakany.property.internal.domain.Building;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record GetBuildingsByCompoundQuery(
    UUID compoundId
) implements Query<List<Building>> {
}
