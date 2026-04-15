package com.theMs.sakany.community.internal.domain;

import com.theMs.sakany.community.internal.domain.events.AlertCreated;
import com.theMs.sakany.community.internal.domain.events.AlertResolved;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Alert extends AggregateRoot {
    private UUID id;
    private UUID reporterId;
    private AlertType type;
    private String title;
    private String description;
    private List<String> photoUrls;
    private boolean isResolved;
    private Instant resolvedAt;

    private Alert(UUID id, UUID reporterId, AlertType type, String title, String description, List<String> photoUrls, boolean isResolved, Instant resolvedAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.photoUrls = photoUrls == null ? new ArrayList<>() : new ArrayList<>(photoUrls);
        this.isResolved = isResolved;
        this.resolvedAt = resolvedAt;
    }

    public static Alert create(UUID reporterId, AlertType type, String title, String description, List<String> photoUrls) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Title cannot be blank");
        }
        if (description == null || description.isBlank()) {
            throw new BusinessRuleException("Description cannot be blank");
        }
        if (reporterId == null || type == null) {
            throw new BusinessRuleException("Reporter ID and Type are required");
        }

        UUID id = UUID.randomUUID();
        Alert alert = new Alert(id, reporterId, type, title, description, photoUrls, false, null);
        alert.registerEvent(new AlertCreated(id, reporterId, type, title, description, alert.getPhotoUrls(), Instant.now()));
        return alert;
    }

    // For mapping from DB entity
    public static Alert reconstitute(UUID id, UUID reporterId, AlertType type, String title, String description, List<String> photoUrls, boolean isResolved, Instant resolvedAt) {
        return new Alert(id, reporterId, type, title, description, photoUrls, isResolved, resolvedAt);
    }

    public void resolve() {
        if (this.isResolved) {
            throw new BusinessRuleException("Alert is already resolved");
        }
        this.isResolved = true;
        this.resolvedAt = Instant.now();
        this.registerEvent(new AlertResolved(this.id, this.resolvedAt, Instant.now()));
    }

    public UUID getId() {
        return id;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public AlertType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPhotoUrls() {
        return new ArrayList<>(photoUrls);
    }

    public boolean isResolved() {
        return isResolved;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
