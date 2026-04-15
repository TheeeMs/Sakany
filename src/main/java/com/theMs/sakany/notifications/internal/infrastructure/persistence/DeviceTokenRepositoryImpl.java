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
        DeviceTokenEntity savedEntity = jpaRepository.save(
            Objects.requireNonNull(mapper.toEntity(deviceToken), "DeviceToken entity cannot be null")
        );

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
