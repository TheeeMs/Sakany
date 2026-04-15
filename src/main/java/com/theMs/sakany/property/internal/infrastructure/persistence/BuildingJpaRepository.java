package com.theMs.sakany.property.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BuildingJpaRepository extends JpaRepository<BuildingEntity, UUID> {
    List<BuildingEntity> findByCompoundId(UUID compoundId);
}
