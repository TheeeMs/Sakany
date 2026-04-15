package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "resident_profiles")
public class ResidentProfileEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "is_owner", nullable = false)
    private boolean isOwner = false;

    public ResidentProfileEntity() {}

    public ResidentProfileEntity(UserEntity user, UUID unitId, LocalDate moveInDate, boolean isOwner) {
        this.user = user;
        this.unitId = unitId;
        this.moveInDate = moveInDate;
        this.isOwner = isOwner;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(UUID unitId) {
        this.unitId = unitId;
    }

    public LocalDate getMoveInDate() {
        return moveInDate;
    }

    public void setMoveInDate(LocalDate moveInDate) {
        this.moveInDate = moveInDate;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
