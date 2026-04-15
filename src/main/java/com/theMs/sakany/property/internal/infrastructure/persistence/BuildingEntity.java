package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.util.UUID;

@Entity
@Table(name = "buildings")
public class BuildingEntity extends BaseEntity {

    @Column(name = "compound_id", nullable = false)
    private UUID compoundId;

    @Column(nullable = false)
    private String name;

    @Column(name = "number_of_floors", nullable = false)
    private int numberOfFloors;

    protected BuildingEntity() {
        // JPA requires no-arg constructor
    }

    public BuildingEntity(UUID compoundId, String name, int numberOfFloors) {
        this.compoundId = compoundId;
        this.name = name;
        this.numberOfFloors = numberOfFloors;
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
