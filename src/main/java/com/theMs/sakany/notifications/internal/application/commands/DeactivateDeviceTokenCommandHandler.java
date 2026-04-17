package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.DeviceToken;
import com.theMs.sakany.notifications.internal.domain.DeviceTokenRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateDeviceTokenCommandHandler implements CommandHandler<DeactivateDeviceTokenCommand, Void> {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeactivateDeviceTokenCommandHandler(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    @Transactional
    public Void handle(DeactivateDeviceTokenCommand command) {
        DeviceToken deviceToken = deviceTokenRepository.findById(command.tokenId())
                .orElseThrow(() -> new NotFoundException("DeviceToken", command.tokenId()));

        if (!deviceToken.getUserId().equals(command.actorUserId())) {
            throw new BusinessRuleException("Device token does not belong to authenticated user");
        }

        deviceToken.deactivate();
        deviceTokenRepository.save(deviceToken);
        return null;
    }
}
