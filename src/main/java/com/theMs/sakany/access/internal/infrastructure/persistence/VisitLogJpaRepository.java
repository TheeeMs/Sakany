package com.theMs.sakany.access.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface VisitLogJpaRepository extends JpaRepository<VisitLogEntity, UUID> {
    List<VisitLogEntity> findByResidentId(UUID residentId);
    List<VisitLogEntity> findByAccessCodeId(UUID accessCodeId);
}
