package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRequestJpaRepository extends JpaRepository<MaintenanceRequestEntity, UUID> {
    List<MaintenanceRequestEntity> findByResidentId(UUID residentId);
    List<MaintenanceRequestEntity> findByStatus(MaintenanceStatus status);
}
