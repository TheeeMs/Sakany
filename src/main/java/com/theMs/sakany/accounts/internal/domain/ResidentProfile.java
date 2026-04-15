package com.theMs.sakany.accounts.internal.domain;

import java.time.LocalDate;
import java.util.UUID;

public class ResidentProfile {
    private UUID id;
    private UUID userId;
    private UUID unitId;
    private LocalDate moveInDate;
    private boolean isOwner;

    public ResidentProfile(UUID id, UUID userId, UUID unitId, LocalDate moveInDate, boolean isOwner) {
        this.id = id;
        this.userId = userId;
        this.unitId = unitId;
        this.moveInDate = moveInDate;
        this.isOwner = isOwner;
    }

    public static ResidentProfile create(UUID userId, UUID unitId, LocalDate moveInDate, boolean isOwner) {
        return new ResidentProfile(UUID.randomUUID(), userId, unitId, moveInDate, isOwner);
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

    public boolean isOwner() {
        return isOwner;
    }
}
