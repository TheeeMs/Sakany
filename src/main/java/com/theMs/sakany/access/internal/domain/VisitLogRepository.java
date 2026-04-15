package com.theMs.sakany.access.internal.domain;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface VisitLogRepository {
    void save(VisitLog visitLog);
    Optional<VisitLog> findById(UUID id);
    List<VisitLog> findByResidentId(UUID residentId);
    List<VisitLog> findByAccessCodeId(UUID accessCodeId);
}
