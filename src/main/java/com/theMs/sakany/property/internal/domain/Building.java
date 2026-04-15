package com.theMs.sakany.property.internal.domain;

import com.theMs.sakany.property.internal.domain.events.BuildingCreated;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class Building extends AggregateRoot {
    private UUID id;
    private UUID compoundId;
    private String name;
    private int numberOfFloors;

    private Building(UUID id, UUID compoundId, String name, int numberOfFloors) {
        this.id = id;
        this.compoundId = compoundId;
        this.name = name;
        this.numberOfFloors = numberOfFloors;
    }

    public static Building create(UUID compoundId, String name, int numberOfFloors) {
        if (compoundId == null) {
            throw new BusinessRuleException("Building compoundId cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessRuleException("Building name cannot be null or empty");
        }
        if (numberOfFloors <= 0) {
            throw new BusinessRuleException("Building must have at least 1 floor");
        }

        UUID id = UUID.randomUUID();
        Building building = new Building(id, compoundId, name, numberOfFloors);
        building.registerEvent(new BuildingCreated(id, compoundId, name, Instant.now()));
        return building;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompoundId() {
        return compoundId;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfFloors() {
        return numberOfFloors;
    }
}
