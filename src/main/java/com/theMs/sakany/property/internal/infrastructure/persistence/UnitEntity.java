package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.UnitType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.UUID;

@Entity
@Table(name = "units")
public class UnitEntity extends BaseEntity {

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "unit_number", nullable = false)
    private String unitNumber;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UnitType type;

    protected UnitEntity() {
        // JPA requires no-arg constructor
    }

    public UnitEntity(UUID buildingId, String unitNumber, int floor, UnitType type) {
        this.buildingId = buildingId;
        this.unitNumber = unitNumber;
        this.floor = floor;
        this.type = type;
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
