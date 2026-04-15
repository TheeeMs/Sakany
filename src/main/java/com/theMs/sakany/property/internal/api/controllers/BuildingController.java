package com.theMs.sakany.property.internal.api.controllers;

import com.theMs.sakany.property.internal.api.dtos.BuildingResponse;
import com.theMs.sakany.property.internal.api.dtos.CreateBuildingRequest;
import com.theMs.sakany.property.internal.application.commands.CreateBuildingCommand;
import com.theMs.sakany.property.internal.application.commands.CreateBuildingCommandHandler;
import com.theMs.sakany.property.internal.application.queries.GetBuildingsByCompoundQuery;
import com.theMs.sakany.property.internal.application.queries.GetBuildingsByCompoundQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/buildings")
public class BuildingController {

    private final CreateBuildingCommandHandler createBuildingCommandHandler;
    private final GetBuildingsByCompoundQueryHandler getBuildingsByCompoundQueryHandler;

    public BuildingController(CreateBuildingCommandHandler createBuildingCommandHandler, GetBuildingsByCompoundQueryHandler getBuildingsByCompoundQueryHandler) {
        this.createBuildingCommandHandler = createBuildingCommandHandler;
        this.getBuildingsByCompoundQueryHandler = getBuildingsByCompoundQueryHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> createBuilding(@RequestBody CreateBuildingRequest request) {
        CreateBuildingCommand command = new CreateBuildingCommand(request.compoundId(), request.name(), request.numberOfFloors());
        UUID id = createBuildingCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/compound/{compoundId}")
    public ResponseEntity<List<BuildingResponse>> getBuildingsByCompound(@PathVariable UUID compoundId) {
        GetBuildingsByCompoundQuery query = new GetBuildingsByCompoundQuery(compoundId);
        List<BuildingResponse> responses = getBuildingsByCompoundQueryHandler.handle(query).stream()
                .map(building -> new BuildingResponse(building.getId(), building.getCompoundId(), building.getName(), building.getNumberOfFloors()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
