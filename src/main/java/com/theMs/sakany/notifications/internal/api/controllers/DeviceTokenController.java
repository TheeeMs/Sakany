package com.theMs.sakany.notifications.internal.api.controllers;

import com.theMs.sakany.notifications.internal.api.dtos.RegisterDeviceTokenRequest;
import com.theMs.sakany.notifications.internal.application.commands.DeactivateDeviceTokenCommand;
import com.theMs.sakany.notifications.internal.application.commands.DeactivateDeviceTokenCommandHandler;
import com.theMs.sakany.notifications.internal.application.commands.RegisterDeviceTokenCommand;
import com.theMs.sakany.notifications.internal.application.commands.RegisterDeviceTokenCommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/device-tokens")
public class DeviceTokenController {

    private final RegisterDeviceTokenCommandHandler registerDeviceTokenCommandHandler;
    private final DeactivateDeviceTokenCommandHandler deactivateDeviceTokenCommandHandler;

    public DeviceTokenController(
            RegisterDeviceTokenCommandHandler registerDeviceTokenCommandHandler,
            DeactivateDeviceTokenCommandHandler deactivateDeviceTokenCommandHandler
    ) {
        this.registerDeviceTokenCommandHandler = registerDeviceTokenCommandHandler;
        this.deactivateDeviceTokenCommandHandler = deactivateDeviceTokenCommandHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> registerDeviceToken(@RequestBody RegisterDeviceTokenRequest request) {
        UUID actorId = resolveUserIdFromAuthentication(request.userId());

        UUID id = registerDeviceTokenCommandHandler.handle(
                new RegisterDeviceTokenCommand(actorId, request.token(), request.platform())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDeviceToken(@PathVariable UUID id) {
        UUID actorId = getAuthenticatedUserId();
        deactivateDeviceTokenCommandHandler.handle(new DeactivateDeviceTokenCommand(id, actorId));
        return ResponseEntity.noContent().build();
    }

    private UUID resolveUserIdFromAuthentication(UUID requestedUserId) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        if (requestedUserId != null && !requestedUserId.equals(authenticatedUserId)) {
            throw new BusinessRuleException("userId must match authenticated user");
        }

        return authenticatedUserId;
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }

        try {
            return UUID.fromString(principal.toString());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid authenticated principal");
        }
    }
}
