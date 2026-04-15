package com.theMs.sakany.notifications.internal.api.dtos;

import com.theMs.sakany.notifications.internal.domain.Platform;

import java.util.UUID;

public record RegisterDeviceTokenRequest(
        UUID userId,
        String token,
        Platform platform
) {
}
