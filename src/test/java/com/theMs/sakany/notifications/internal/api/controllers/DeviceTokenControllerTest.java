package com.theMs.sakany.notifications.internal.api.controllers;

import com.theMs.sakany.notifications.internal.api.dtos.RegisterDeviceTokenRequest;
import com.theMs.sakany.notifications.internal.application.commands.DeactivateDeviceTokenCommand;
import com.theMs.sakany.notifications.internal.application.commands.DeactivateDeviceTokenCommandHandler;
import com.theMs.sakany.notifications.internal.application.commands.RegisterDeviceTokenCommand;
import com.theMs.sakany.notifications.internal.application.commands.RegisterDeviceTokenCommandHandler;
import com.theMs.sakany.notifications.internal.domain.Platform;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenControllerTest {

    @Mock
    private RegisterDeviceTokenCommandHandler registerDeviceTokenCommandHandler;

    @Mock
    private DeactivateDeviceTokenCommandHandler deactivateDeviceTokenCommandHandler;

    private DeviceTokenController controller;

    @BeforeEach
    void setUp() {
        controller = new DeviceTokenController(registerDeviceTokenCommandHandler, deactivateDeviceTokenCommandHandler);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerDeviceToken_shouldRejectMismatchedUserId() {
        UUID actorId = UUID.randomUUID();
        setAuthentication(actorId);

        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(
                UUID.randomUUID(),
                "token-value",
                Platform.ANDROID
        );

        assertThrows(BusinessRuleException.class, () -> controller.registerDeviceToken(request));
    }

    @Test
    void registerDeviceToken_shouldUseAuthenticatedUserWhenMissing() {
        UUID actorId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        setAuthentication(actorId);
        when(registerDeviceTokenCommandHandler.handle(any(RegisterDeviceTokenCommand.class))).thenReturn(tokenId);

        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest(
                null,
                "token-value",
                Platform.ANDROID
        );

        controller.registerDeviceToken(request);

        ArgumentCaptor<RegisterDeviceTokenCommand> captor = ArgumentCaptor.forClass(RegisterDeviceTokenCommand.class);
        verify(registerDeviceTokenCommandHandler).handle(captor.capture());
        assertEquals(actorId, captor.getValue().userId());
    }

    @Test
    void deactivateDeviceToken_shouldPassAuthenticatedUserToCommand() {
        UUID actorId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        setAuthentication(actorId);

        controller.deactivateDeviceToken(tokenId);

        ArgumentCaptor<DeactivateDeviceTokenCommand> captor = ArgumentCaptor.forClass(DeactivateDeviceTokenCommand.class);
        verify(deactivateDeviceTokenCommandHandler).handle(captor.capture());
        assertEquals(tokenId, captor.getValue().tokenId());
        assertEquals(actorId, captor.getValue().actorUserId());
    }

    private void setAuthentication(UUID principalId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principalId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_RESIDENT"))
                )
        );
    }
}