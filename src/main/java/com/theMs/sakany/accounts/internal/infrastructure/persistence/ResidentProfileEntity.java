package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.accounts.internal.domain.ResidentApprovalStatus;
import com.theMs.sakany.accounts.internal.domain.ResidentType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
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

    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Column(name = "monthly_fee", precision = 12, scale = 2)
    private BigDecimal monthlyFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "resident_type", nullable = false)
    private ResidentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ResidentApprovalStatus approvalStatus = ResidentApprovalStatus.PENDING;

    public ResidentProfileEntity() {}

    public ResidentProfileEntity(UserEntity user, UUID unitId, LocalDate moveInDate, ResidentType type) {
        this.user = user;
        this.unitId = unitId;
        this.moveInDate = moveInDate;
        this.type = type;
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

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public ResidentType getType() {
        return type;
    }

    public void setType(ResidentType type) {
        this.type = type;
    }

    public ResidentApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ResidentApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
}
