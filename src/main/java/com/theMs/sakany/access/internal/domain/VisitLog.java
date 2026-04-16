package com.theMs.sakany.access.internal.domain;

import com.theMs.sakany.access.internal.domain.events.VisitorEntryLogged;
import com.theMs.sakany.access.internal.domain.events.VisitorExitLogged;
import com.theMs.sakany.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class VisitLog extends AggregateRoot {
    private UUID id;
    private UUID accessCodeId;
    private UUID residentId;
    private String visitorName;
    private Instant entryTime;
    private Instant exitTime;
    private String gateNumber;

    private VisitLog(
        UUID id,
        UUID accessCodeId,
        UUID residentId,
        String visitorName,
        Instant entryTime,
        Instant exitTime,
        String gateNumber
    ) {
        this.id = id;
        this.accessCodeId = accessCodeId;
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.gateNumber = gateNumber;
    }

    public static VisitLog create(
        UUID accessCodeId,
        UUID residentId,
        String visitorName,
        String gateNumber
    ) {
        Instant now = Instant.now();
        
        VisitLog visitLog = new VisitLog(
            UUID.randomUUID(),
            accessCodeId,
            residentId,
            visitorName,
            now,
            null,
            gateNumber
        );

        visitLog.registerEvent(new VisitorEntryLogged(
            visitLog.id,
            accessCodeId,
            residentId,
            visitorName,
            now,
            now
        ));

        return visitLog;
    }

    public static VisitLog rehydrate(
        UUID id,
        UUID accessCodeId,
        UUID residentId,
        String visitorName,
        Instant entryTime,
        Instant exitTime,
        String gateNumber
    ) {
        return new VisitLog(
            id,
            accessCodeId,
            residentId,
            visitorName,
            entryTime,
            exitTime,
            gateNumber
        );
    }

    /**
     * Log the visitor's exit time.
     */
    public void logExit() {
        if (this.exitTime != null) {
            throw new IllegalStateException("Exit time has already been logged for this visit");
        }

        Instant now = Instant.now();
        this.exitTime = now;

        registerEvent(new VisitorExitLogged(
            this.id,
            this.residentId,
            now,
            now
        ));
    }

    public boolean hasExited() {
        return exitTime != null;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getAccessCodeId() {
        return accessCodeId;
    }

    public UUID getResidentId() {
        return residentId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public String getGateNumber() {
        return gateNumber;
    }
}
