package com.theMs.sakany.property.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CompoundJpaRepository extends JpaRepository<CompoundEntity, UUID> {
}
