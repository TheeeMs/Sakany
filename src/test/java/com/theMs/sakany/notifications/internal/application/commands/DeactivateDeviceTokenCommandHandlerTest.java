package com.theMs.sakany.notifications.internal.application.commands;

import com.theMs.sakany.notifications.internal.domain.DeviceToken;
import com.theMs.sakany.notifications.internal.domain.DeviceTokenRepository;
import com.theMs.sakany.notifications.internal.domain.Platform;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateDeviceTokenCommandHandlerTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Test
    void handle_shouldRejectWhenTokenBelongsToAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        DeviceToken token = DeviceToken.create(ownerId, "token-value", Platform.ANDROID);

        when(deviceTokenRepository.findById(token.getId())).thenReturn(Optional.of(token));

        DeactivateDeviceTokenCommandHandler handler = new DeactivateDeviceTokenCommandHandler(deviceTokenRepository);

        assertThrows(BusinessRuleException.class, () ->
                handler.handle(new DeactivateDeviceTokenCommand(token.getId(), actorId))
        );

        verify(deviceTokenRepository, never()).save(token);
    }

    @Test
    void handle_shouldDeactivateOwnedToken() {
        UUID actorId = UUID.randomUUID();
        DeviceToken token = DeviceToken.create(actorId, "token-value", Platform.ANDROID);

        when(deviceTokenRepository.findById(token.getId())).thenReturn(Optional.of(token));
        when(deviceTokenRepository.save(token)).thenReturn(token);

        DeactivateDeviceTokenCommandHandler handler = new DeactivateDeviceTokenCommandHandler(deviceTokenRepository);
        handler.handle(new DeactivateDeviceTokenCommand(token.getId(), actorId));

        assertFalse(token.isActive());
        verify(deviceTokenRepository).save(token);
    }
}