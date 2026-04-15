package com.theMs.sakany.accounts.internal.domain;

import java.util.List;
import java.util.UUID;

public class AdminProfile {
    private UUID id;
    private UUID userId;
    private List<String> scopePermissions;

    public AdminProfile(UUID id, UUID userId, List<String> scopePermissions) {
        this.id = id;
        this.userId = userId;
        this.scopePermissions = scopePermissions;
    }

    public static AdminProfile create(UUID userId, List<String> scopePermissions) {
        return new AdminProfile(UUID.randomUUID(), userId, scopePermissions);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<String> getScopePermissions() {
        return scopePermissions;
    }
}
