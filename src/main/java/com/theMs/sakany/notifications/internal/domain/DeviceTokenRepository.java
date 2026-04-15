package com.theMs.sakany.notifications.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository {
    DeviceToken save(DeviceToken deviceToken);

    Optional<DeviceToken> findById(UUID id);

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserIdAndActive(UUID userId);
}
