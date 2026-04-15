package com.theMs.sakany.accounts.internal.domain;

import java.util.List;
import java.util.UUID;

public class TechnicianProfile {
    private UUID id;
    private UUID userId;
    private List<String> specializations;
    private boolean isAvailable;
    private Double rating;

    public TechnicianProfile(UUID id, UUID userId, List<String> specializations, boolean isAvailable, Double rating) {
        this.id = id;
        this.userId = userId;
        this.specializations = specializations;
        this.isAvailable = isAvailable;
        this.rating = rating;
    }

    public static TechnicianProfile create(UUID userId, List<String> specializations) {
        return new TechnicianProfile(UUID.randomUUID(), userId, specializations, true, null);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Double getRating() {
        return rating;
    }
}
