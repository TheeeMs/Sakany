package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.DeviceToken;
import com.theMs.sakany.notifications.internal.domain.DeviceTokenRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterDeviceTokenCommandHandler implements CommandHandler<RegisterDeviceTokenCommand, UUID> {

    private final DeviceTokenRepository deviceTokenRepository;

    public RegisterDeviceTokenCommandHandler(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    @Transactional
    public UUID handle(RegisterDeviceTokenCommand command) {
        DeviceToken deviceToken = deviceTokenRepository.findByToken(command.token())
                .map(existing -> {
                    existing.refresh(command.token());
                    return existing;
                })
                .orElseGet(() -> DeviceToken.create(command.userId(), command.token(), command.platform()));

        DeviceToken saved = deviceTokenRepository.save(deviceToken);
        return saved.getId();
    }
}
