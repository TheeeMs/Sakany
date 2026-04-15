package com.theMs.sakany.property.internal.domain;

import java.util.Optional;
import java.util.UUID;

public interface CompoundRepository {
    Compound save(Compound compound);
    Optional<Compound> findById(UUID id);
}
