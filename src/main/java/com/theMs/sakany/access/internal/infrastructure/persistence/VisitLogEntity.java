package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visit_logs")
public class VisitLogEntity extends BaseEntity {

    @Column(name = "access_code_id", nullable = false)
    private UUID accessCodeId;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "visitor_name", nullable = false, length = 255)
    private String visitorName;

    @Column(name = "entry_time", nullable = false)
    private Instant entryTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "gate_number", length = 10)
    private String gateNumber;

    public VisitLogEntity() {}

    public VisitLogEntity(
        UUID accessCodeId,
        UUID residentId,
        String visitorName,
        Instant entryTime,
        Instant exitTime,
        String gateNumber
    ) {
        this.accessCodeId = accessCodeId;
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.gateNumber = gateNumber;
    }

    // Getters and Setters
    public UUID getAccessCodeId() {
        return accessCodeId;
    }

    public void setAccessCodeId(UUID accessCodeId) {
        this.accessCodeId = accessCodeId;
    }

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

    public Instant getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Instant entryTime) {
        this.entryTime = entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public void setExitTime(Instant exitTime) {
        this.exitTime = exitTime;
    }

    public String getGateNumber() {
        return gateNumber;
    }

    public void setGateNumber(String gateNumber) {
        this.gateNumber = gateNumber;
    }
}
