package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.Platform;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record RegisterDeviceTokenCommand(
        UUID userId,
        String token,
        Platform platform
) implements Command<UUID> {
}
