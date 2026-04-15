package com.theMs.sakany.notifications.internal.domain;

import com.theMs.sakany.notifications.internal.domain.events.DeviceTokenDeactivated;
import com.theMs.sakany.notifications.internal.domain.events.DeviceTokenRegistered;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class DeviceToken extends AggregateRoot {
    private UUID id;
    private UUID userId;
    private String token;
    private Platform platform;
    private boolean isActive;
    private Instant lastUsedAt;

    private DeviceToken(
            UUID id,
            UUID userId,
            String token,
            Platform platform,
            boolean isActive,
            Instant lastUsedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.isActive = isActive;
        this.lastUsedAt = lastUsedAt;
    }

    public static DeviceToken create(UUID userId, String token, Platform platform) {
        if (userId == null) {
            throw new BusinessRuleException("Device token userId cannot be null");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessRuleException("Device token value cannot be null or empty");
        }
        if (platform == null) {
            throw new BusinessRuleException("Device token platform cannot be null");
        }

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        DeviceToken deviceToken = new DeviceToken(id, userId, token.trim(), platform, true, now);
        deviceToken.registerEvent(new DeviceTokenRegistered(id, userId, token.trim(), platform, now));
        return deviceToken;
    }

    public void deactivate() {
        if (!isActive) {
            throw new BusinessRuleException("Device token is already inactive");
        }
        this.isActive = false;
        registerEvent(new DeviceTokenDeactivated(id, userId));
    }

    public void refresh(String newToken) {
        if (newToken == null || newToken.trim().isEmpty()) {
            throw new BusinessRuleException("New token cannot be null or empty");
        }
        this.token = newToken.trim();
        this.isActive = true;
        this.lastUsedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public Platform getPlatform() {
        return platform;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }
}
