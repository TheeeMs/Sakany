package com.theMs.sakany.notifications.internal.infrastructure.persistence;

import com.theMs.sakany.notifications.internal.domain.DeviceToken;
import com.theMs.sakany.notifications.internal.domain.Platform;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

@Component
public class DeviceTokenMapper {

    public DeviceTokenEntity toEntity(DeviceToken deviceToken) {
        if (deviceToken == null) {
            return null;
        }

        DeviceTokenEntity entity = new DeviceTokenEntity(
                deviceToken.getUserId(),
                deviceToken.getToken(),
                deviceToken.getPlatform(),
                deviceToken.isActive(),
                deviceToken.getLastUsedAt()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, deviceToken.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on DeviceTokenEntity", e);
        }

        return entity;
    }

    public DeviceToken toDomain(DeviceTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<DeviceToken> constructor = DeviceToken.class.getDeclaredConstructor(
                    UUID.class,
                    UUID.class,
                    String.class,
                    Platform.class,
                    boolean.class,
                    Instant.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(),
                    entity.getUserId(),
                    entity.getToken(),
                    entity.getPlatform(),
                    entity.isActive(),
                    entity.getLastUsedAt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map DeviceTokenEntity to domain", e);
        }
    }
}
