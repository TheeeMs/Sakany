package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record DeactivateDeviceTokenCommand(UUID tokenId) implements Command<Void> {
}
