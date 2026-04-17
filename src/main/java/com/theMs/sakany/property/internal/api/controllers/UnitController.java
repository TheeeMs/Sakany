package com.theMs.sakany.property.internal.api.controllers;

import com.theMs.sakany.property.internal.api.dtos.CreateUnitRequest;
import com.theMs.sakany.property.internal.api.dtos.UnitResponse;
import com.theMs.sakany.property.internal.application.commands.CreateUnitCommand;
import com.theMs.sakany.property.internal.application.commands.CreateUnitCommandHandler;
import com.theMs.sakany.property.internal.application.queries.GetUnitsByBuildingQuery;
import com.theMs.sakany.property.internal.application.queries.GetUnitsByBuildingQueryHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/units")
public class UnitController {

    private final CreateUnitCommandHandler createUnitCommandHandler;
    private final GetUnitsByBuildingQueryHandler getUnitsByBuildingQueryHandler;

    public UnitController(CreateUnitCommandHandler createUnitCommandHandler, GetUnitsByBuildingQueryHandler getUnitsByBuildingQueryHandler) {
        this.createUnitCommandHandler = createUnitCommandHandler;
        this.getUnitsByBuildingQueryHandler = getUnitsByBuildingQueryHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> createUnit(@Valid @RequestBody CreateUnitRequest request) {
        CreateUnitCommand command = new CreateUnitCommand(request.buildingId(), request.unitNumber(), request.floor(), request.type());
        UUID id = createUnitCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/building/{buildingId}")
    public ResponseEntity<List<UnitResponse>> getUnitsByBuilding(@PathVariable UUID buildingId) {
        GetUnitsByBuildingQuery query = new GetUnitsByBuildingQuery(buildingId);
        List<UnitResponse> responses = getUnitsByBuildingQueryHandler.handle(query).stream()
                .map(unit -> new UnitResponse(unit.getId(), unit.getBuildingId(), unit.getUnitNumber(), unit.getFloor(), unit.getType()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
