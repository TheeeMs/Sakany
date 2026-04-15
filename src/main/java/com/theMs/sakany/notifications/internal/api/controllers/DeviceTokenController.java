package com.theMs.sakany.notifications.internal.api.controllers;

import com.theMs.sakany.notifications.internal.api.dtos.RegisterDeviceTokenRequest;
import com.theMs.sakany.notifications.internal.application.commands.DeactivateDeviceTokenCommand;
import com.theMs.sakany.notifications.internal.application.commands.DeactivateDeviceTokenCommandHandler;
import com.theMs.sakany.notifications.internal.application.commands.RegisterDeviceTokenCommand;
import com.theMs.sakany.notifications.internal.application.commands.RegisterDeviceTokenCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        UUID id = registerDeviceTokenCommandHandler.handle(
                new RegisterDeviceTokenCommand(request.userId(), request.token(), request.platform())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDeviceToken(@PathVariable UUID id) {
        deactivateDeviceTokenCommandHandler.handle(new DeactivateDeviceTokenCommand(id));
        return ResponseEntity.noContent().build();
    }
}
