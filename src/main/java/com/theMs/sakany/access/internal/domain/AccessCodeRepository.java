package com.theMs.sakany.access.internal.domain;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface AccessCodeRepository {
    AccessCode save(AccessCode accessCode);
    Optional<AccessCode> findById(UUID id);
    Optional<AccessCode> findByCode(String code);
    List<AccessCode> findByResidentId(UUID residentId);
    void delete(UUID id);
}
