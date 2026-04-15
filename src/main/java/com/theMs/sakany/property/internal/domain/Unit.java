package com.theMs.sakany.property.internal.domain;

import com.theMs.sakany.property.internal.domain.events.UnitCreated;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class Unit extends AggregateRoot {
    private UUID id;
    private UUID buildingId;
    private String unitNumber;
    private int floor;
    private UnitType type;

    private Unit(UUID id, UUID buildingId, String unitNumber, int floor, UnitType type) {
        this.id = id;
        this.buildingId = buildingId;
        this.unitNumber = unitNumber;
        this.floor = floor;
        this.type = type;
    }

    public static Unit create(UUID buildingId, String unitNumber, int floor, UnitType type) {
        if (buildingId == null) {
            throw new BusinessRuleException("Unit buildingId cannot be null");
        }
        if (unitNumber == null || unitNumber.trim().isEmpty()) {
            throw new BusinessRuleException("Unit number cannot be null or empty");
        }
        if (floor < 0) {
            throw new BusinessRuleException("Unit floor cannot be negative");
        }
        if (type == null) {
            throw new BusinessRuleException("Unit type cannot be null");
        }

        UUID id = UUID.randomUUID();
        Unit unit = new Unit(id, buildingId, unitNumber, floor, type);
        unit.registerEvent(new UnitCreated(id, buildingId, unitNumber, Instant.now()));
        return unit;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuildingId() {
        return buildingId;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public int getFloor() {
        return floor;
    }

    public UnitType getType() {
        return type;
    }
}
