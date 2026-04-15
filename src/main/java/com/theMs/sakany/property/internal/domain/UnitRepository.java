package com.theMs.sakany.property.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitRepository {
    Unit save(Unit unit);
    Optional<Unit> findById(UUID id);
    List<Unit> findByBuildingId(UUID buildingId);
}
