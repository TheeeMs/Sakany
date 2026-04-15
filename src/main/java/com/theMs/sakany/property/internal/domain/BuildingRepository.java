package com.theMs.sakany.property.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository {
    Building save(Building building);
    Optional<Building> findById(UUID id);
    List<Building> findByCompoundId(UUID compoundId);
}
