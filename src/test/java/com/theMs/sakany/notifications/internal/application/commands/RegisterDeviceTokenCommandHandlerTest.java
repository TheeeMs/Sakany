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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterDeviceTokenCommandHandlerTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Test
    void handle_shouldRejectExistingTokenOwnedByAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        DeviceToken existing = DeviceToken.create(ownerId, "same-token", Platform.ANDROID);

        when(deviceTokenRepository.findByToken("same-token")).thenReturn(Optional.of(existing));

        RegisterDeviceTokenCommandHandler handler = new RegisterDeviceTokenCommandHandler(deviceTokenRepository);

        assertThrows(BusinessRuleException.class, () ->
                handler.handle(new RegisterDeviceTokenCommand(actorId, "same-token", Platform.ANDROID))
        );

        verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
    }

    @Test
    void handle_shouldCreateNewTokenWhenNotExisting() {
        UUID actorId = UUID.randomUUID();

        when(deviceTokenRepository.findByToken("new-token")).thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterDeviceTokenCommandHandler handler = new RegisterDeviceTokenCommandHandler(deviceTokenRepository);
        UUID savedId = handler.handle(new RegisterDeviceTokenCommand(actorId, "new-token", Platform.ANDROID));

        assertNotNull(savedId);
        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }
}