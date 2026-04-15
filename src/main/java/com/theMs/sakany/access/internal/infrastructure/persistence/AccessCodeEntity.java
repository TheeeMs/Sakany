package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_codes")
public class AccessCodeEntity extends BaseEntity {

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "visitor_name", nullable = false, length = 255)
    private String visitorName;

    @Column(name = "visitor_phone", length = 20)
    private String visitorPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 50)
    private com.theMs.sakany.access.internal.domain.VisitPurpose purpose;

    @Column(name = "code", nullable = false, unique = true, length = 8)
    private String code;

    @Column(name = "qr_data", nullable = false, columnDefinition = "TEXT")
    private String qrData;

    @Column(name = "is_single_use", nullable = false)
    private boolean isSingleUse;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private com.theMs.sakany.access.internal.domain.AccessCodeStatus status;

    @Column(name = "used_at")
    private Instant usedAt;

    public AccessCodeEntity() {}

    public AccessCodeEntity(
        UUID residentId,
        String visitorName,
        String visitorPhone,
        com.theMs.sakany.access.internal.domain.VisitPurpose purpose,
        String code,
        String qrData,
        boolean isSingleUse,
        Instant validFrom,
        Instant validUntil,
        com.theMs.sakany.access.internal.domain.AccessCodeStatus status,
        Instant usedAt
    ) {
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.visitorPhone = visitorPhone;
        this.purpose = purpose;
        this.code = code;
        this.qrData = qrData;
        this.isSingleUse = isSingleUse;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = status;
        this.usedAt = usedAt;
    }

    // Getters and Setters
    public UUID getResidentId() {
        return residentId;
    }

    public void setResidentId(UUID residentId) {
        this.residentId = residentId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorPhone() {
        return visitorPhone;
    }

    public void setVisitorPhone(String visitorPhone) {
        this.visitorPhone = visitorPhone;
    }

    public com.theMs.sakany.access.internal.domain.VisitPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(com.theMs.sakany.access.internal.domain.VisitPurpose purpose) {
        this.purpose = purpose;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }

    public boolean isSingleUse() {
        return isSingleUse;
    }

    public void setSingleUse(boolean singleUse) {
        isSingleUse = singleUse;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public com.theMs.sakany.access.internal.domain.AccessCodeStatus getStatus() {
        return status;
    }

    public void setStatus(com.theMs.sakany.access.internal.domain.AccessCodeStatus status) {
        this.status = status;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }
}
