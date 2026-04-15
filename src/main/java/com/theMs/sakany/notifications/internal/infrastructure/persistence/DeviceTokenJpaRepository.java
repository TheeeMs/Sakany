package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenEntity, UUID> {
    Optional<DeviceTokenEntity> findByToken(String token);

    List<DeviceTokenEntity> findByUserIdAndIsActiveTrue(UUID userId);
}
