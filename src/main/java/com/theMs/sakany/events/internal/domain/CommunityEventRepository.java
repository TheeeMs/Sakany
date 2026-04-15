package com.theMs.sakany.events.internal.domain;

import java.util.Optional;
import java.util.UUID;

public interface CommunityEventRepository {
    CommunityEvent save(CommunityEvent event);
    Optional<CommunityEvent> findById(UUID id);
}
