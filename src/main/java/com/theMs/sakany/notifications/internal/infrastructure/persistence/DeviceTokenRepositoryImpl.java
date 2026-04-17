package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import com.theMs.sakany.notifications.internal.domain.DeviceToken;
import com.theMs.sakany.notifications.internal.domain.DeviceTokenRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DeviceTokenRepositoryImpl implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository jpaRepository;
    private final DeviceTokenMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public DeviceTokenRepositoryImpl(
            DeviceTokenJpaRepository jpaRepository,
            DeviceTokenMapper mapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        Objects.requireNonNull(deviceToken, "DeviceToken cannot be null");

        DeviceTokenEntity entity = deviceToken.getId() == null
                ? new DeviceTokenEntity()
                : jpaRepository.findById(deviceToken.getId()).orElseGet(DeviceTokenEntity::new);

        entity.setId(deviceToken.getId());
        entity.setUserId(deviceToken.getUserId());
        entity.setToken(deviceToken.getToken());
        entity.setPlatform(deviceToken.getPlatform());
        entity.setActive(deviceToken.isActive());
        entity.setLastUsedAt(deviceToken.getLastUsedAt());

        DeviceTokenEntity savedEntity = jpaRepository.save(entity);

        deviceToken.getDomainEvents().forEach(eventPublisher::publishEvent);
        deviceToken.clearDomainEvents();

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<DeviceToken> findById(UUID id) {
        Objects.requireNonNull(id, "DeviceToken id cannot be null");
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<DeviceToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public List<DeviceToken> findByUserIdAndActive(UUID userId) {
        return jpaRepository.findByUserIdAndIsActiveTrue(userId).stream().map(mapper::toDomain).toList();
    }
}
