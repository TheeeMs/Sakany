package com.theMs.sakany.access.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface AccessCodeJpaRepository extends JpaRepository<AccessCodeEntity, UUID> {
    Optional<AccessCodeEntity> findByCode(String code);
    List<AccessCodeEntity> findByResidentId(UUID residentId);
}
