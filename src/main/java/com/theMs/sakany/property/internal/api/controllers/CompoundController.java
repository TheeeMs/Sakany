package com.theMs.sakany.property.internal.api.controllers;

import com.theMs.sakany.property.internal.api.dtos.CompoundResponse;
import com.theMs.sakany.property.internal.api.dtos.CreateCompoundRequest;
import com.theMs.sakany.property.internal.application.commands.CreateCompoundCommand;
import com.theMs.sakany.property.internal.application.commands.CreateCompoundCommandHandler;
import com.theMs.sakany.property.internal.application.queries.GetCompoundByIdQuery;
import com.theMs.sakany.property.internal.application.queries.GetCompoundByIdQueryHandler;
import com.theMs.sakany.property.internal.domain.Compound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/compounds")
public class CompoundController {

    private final CreateCompoundCommandHandler createCompoundCommandHandler;
    private final GetCompoundByIdQueryHandler getCompoundByIdQueryHandler;

    public CompoundController(CreateCompoundCommandHandler createCompoundCommandHandler, GetCompoundByIdQueryHandler getCompoundByIdQueryHandler) {
        this.createCompoundCommandHandler = createCompoundCommandHandler;
        this.getCompoundByIdQueryHandler = getCompoundByIdQueryHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> createCompound(@RequestBody CreateCompoundRequest request) {
        CreateCompoundCommand command = new CreateCompoundCommand(request.name(), request.address());
        UUID id = createCompoundCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompoundResponse> getCompoundById(@PathVariable UUID id) {
        GetCompoundByIdQuery query = new GetCompoundByIdQuery(id);
        Compound compound = getCompoundByIdQueryHandler.handle(query);
        CompoundResponse response = new CompoundResponse(compound.getId(), compound.getName(), compound.getAddress());
        return ResponseEntity.ok(response);
    }
}
