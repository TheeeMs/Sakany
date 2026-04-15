package com.theMs.sakany.maintenance.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceRequestRepository {
    MaintenanceRequest save(MaintenanceRequest request);
    Optional<MaintenanceRequest> findById(UUID id);
    List<MaintenanceRequest> findByResidentId(UUID residentId);
    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);
}
