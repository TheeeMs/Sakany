package com.theMs.sakany.accounts.internal.domain;

import java.time.LocalDate;
import java.util.UUID;

public class ResidentProfile {
    private UUID id;
    private UUID userId;
    private UUID unitId;
    private LocalDate moveInDate;
    private ResidentType type;

    public ResidentProfile(UUID id, UUID userId, UUID unitId, LocalDate moveInDate, ResidentType type) {
        this.id = id;
        this.userId = userId;
        this.unitId = unitId;
        this.moveInDate = moveInDate;
        this.type = type;
    }

    public static ResidentProfile create(UUID userId, UUID unitId, LocalDate moveInDate, ResidentType type) {
        return new ResidentProfile(UUID.randomUUID(), userId, unitId, moveInDate, type);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public LocalDate getMoveInDate() {
        return moveInDate;
    }

    public ResidentType getType() {
        return type;
    }
}
