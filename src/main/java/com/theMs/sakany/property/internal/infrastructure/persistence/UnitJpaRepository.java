package com.theMs.sakany.property.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UnitJpaRepository extends JpaRepository<UnitEntity, UUID> {
    List<UnitEntity> findByBuildingId(UUID buildingId);
}
