package com.theMs.sakany.access.internal.domain;

import com.theMs.sakany.access.internal.domain.events.AccessCodeCreated;
import com.theMs.sakany.access.internal.domain.events.AccessCodeRevoked;
import com.theMs.sakany.access.internal.domain.events.AccessCodeUsed;
import com.theMs.sakany.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class AccessCode extends AggregateRoot {
    private UUID id;
    private UUID residentId;
    private String visitorName;
    private String visitorPhone;
    private VisitPurpose purpose;
    private String code;
    private String qrData;
    private boolean isSingleUse;
    private Instant validFrom;
    private Instant validUntil;
    private AccessCodeStatus status;
    private Instant usedAt;

    private AccessCode(
        UUID id,
        UUID residentId,
        String visitorName,
        String visitorPhone,
        VisitPurpose purpose,
        String code,
        String qrData,
        boolean isSingleUse,
        Instant validFrom,
        Instant validUntil,
        AccessCodeStatus status,
        Instant usedAt
    ) {
        this.id = id;
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

    public static AccessCode create(
        UUID residentId,
        String visitorName,
        String visitorPhone,
        VisitPurpose purpose,
        String code,
        String qrData,
        boolean isSingleUse,
        Instant validFrom,
        Instant validUntil
    ) {
        // Validate invariants
        if (validUntil.isBefore(validFrom) || validUntil.equals(validFrom)) {
            throw new IllegalArgumentException("validUntil must be after validFrom");
        }

        AccessCode accessCode = new AccessCode(
            UUID.randomUUID(),
            residentId,
            visitorName,
            visitorPhone,
            purpose,
            code,
            qrData,
            isSingleUse,
            validFrom,
            validUntil,
            AccessCodeStatus.ACTIVE,
            null
        );

        accessCode.registerEvent(new AccessCodeCreated(
            accessCode.id,
            residentId,
            visitorName,
            code,
            validFrom,
            validUntil,
            Instant.now()
        ));

        return accessCode;
    }

    /**
     * Use (scan) this access code. Validates state and applies single-use logic.
     * @throws IllegalStateException if code is expired, revoked, or already used (single-use)
     * @throws IllegalArgumentException if current time is outside valid window
     */
    public void use() {
        Instant now = Instant.now();

        // Validate not expired
        if (status == AccessCodeStatus.EXPIRED) {
            throw new IllegalStateException("Cannot use an EXPIRED access code");
        }

        // Validate not revoked
        if (status == AccessCodeStatus.REVOKED) {
            throw new IllegalStateException("Cannot use a REVOKED access code");
        }

        // Validate not already used (for single-use codes)
        if (isSingleUse && status == AccessCodeStatus.USED) {
            throw new IllegalStateException("This single-use access code has already been used");
        }

        // Validate current time is within valid window
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            throw new IllegalArgumentException("Current time is outside the valid window for this access code");
        }

        // Mark as used
        this.status = isSingleUse ? AccessCodeStatus.USED : AccessCodeStatus.ACTIVE;
        this.usedAt = now;

        registerEvent(new AccessCodeUsed(
            this.id,
            this.residentId,
            now,
            now
        ));
    }

    /**
     * Revoke this access code, preventing any further use.
     */
    public void revoke() {
        if (status == AccessCodeStatus.REVOKED) {
            throw new IllegalStateException("Access code is already revoked");
        }

        this.status = AccessCodeStatus.REVOKED;

        registerEvent(new AccessCodeRevoked(
            this.id,
            this.residentId,
            Instant.now()
        ));
    }

    /**
     * Mark this access code as expired if validUntil has passed.
     */
    public void markAsExpiredIfNeeded() {
        if (status == AccessCodeStatus.ACTIVE && Instant.now().isAfter(validUntil)) {
            this.status = AccessCodeStatus.EXPIRED;
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getResidentId() {
        return residentId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public String getVisitorPhone() {
        return visitorPhone;
    }

    public VisitPurpose getPurpose() {
        return purpose;
    }

    public String getCode() {
        return code;
    }

    public String getQrData() {
        return qrData;
    }

    public boolean isSingleUse() {
        return isSingleUse;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public AccessCodeStatus getStatus() {
        return status;
    }

    public Instant getUsedAt() {
        return usedAt;
    }
}
