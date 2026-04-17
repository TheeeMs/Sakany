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
    private AlertCategory category;
    private String title;
    private String description;
    private String location;
    private Instant eventTime;
    private List<String> photoUrls;
    private boolean isResolved;
    private Instant resolvedAt;
    private AlertReportStatus status;
    private String contactNumber;

    private Alert(
            UUID id,
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            boolean isResolved,
            Instant resolvedAt,
            AlertReportStatus status,
            String contactNumber
    ) {
        this.id = id;
        this.reporterId = reporterId;
        this.type = type;
        this.category = category;
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventTime = eventTime;
        this.photoUrls = photoUrls == null ? new ArrayList<>() : new ArrayList<>(photoUrls);
        this.isResolved = isResolved;
        this.resolvedAt = resolvedAt;
        this.status = status != null ? status : (isResolved ? AlertReportStatus.RESOLVED : AlertReportStatus.OPEN);
        this.contactNumber = normalizeContactNumber(contactNumber);
    }

    public static Alert create(
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls
    ) {
        return create(reporterId, type, category, title, description, location, eventTime, photoUrls, null);
    }

    public static Alert create(
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            String contactNumber
    ) {
        validateRequiredFields(reporterId, type, category, title, description);

        UUID alertId = UUID.randomUUID();
        Alert alert = new Alert(
                alertId,
                reporterId,
                type,
                category,
                title,
                description,
                location,
                eventTime,
                photoUrls,
                false,
                null,
                AlertReportStatus.OPEN,
                contactNumber
        );

        alert.registerEvent(new AlertCreated(
                alertId,
                reporterId,
                type,
                category,
                title,
                description,
                location,
                eventTime,
                alert.getPhotoUrls(),
                Instant.now()
        ));

        return alert;
    }

    public static Alert reconstitute(
            UUID id,
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            boolean isResolved,
            Instant resolvedAt
    ) {
        return reconstitute(id, reporterId, type, category, title, description, location, eventTime, photoUrls, isResolved, resolvedAt, null, null);
    }

    public static Alert reconstitute(
            UUID id,
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            boolean isResolved,
            Instant resolvedAt,
            AlertReportStatus status,
            String contactNumber
    ) {
        return new Alert(
                id,
                reporterId,
                type,
                category,
                title,
                description,
                location,
                eventTime,
                photoUrls,
                isResolved,
                resolvedAt,
                status,
                contactNumber
        );
    }

    public void resolve() {
        if (this.isResolved) {
            throw new BusinessRuleException("Alert is already resolved");
        }

        this.isResolved = true;
        this.status = AlertReportStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        registerEvent(new AlertResolved(this.id, this.resolvedAt, Instant.now()));
    }

    public void updateStatus(AlertReportStatus newStatus) {
        if (newStatus == null) {
            throw new BusinessRuleException("Status is required");
        }

        if (this.status == newStatus) {
            return;
        }

        switch (newStatus) {
            case OPEN -> {
                this.status = AlertReportStatus.OPEN;
                this.isResolved = false;
                this.resolvedAt = null;
            }
            case MATCHED -> {
                this.status = AlertReportStatus.MATCHED;
                this.isResolved = false;
                this.resolvedAt = null;
            }
            case RESOLVED -> {
                if (!this.isResolved) {
                    resolve();
                } else {
                    this.status = AlertReportStatus.RESOLVED;
                    if (this.resolvedAt == null) {
                        this.resolvedAt = Instant.now();
                    }
                }
            }
        }
    }

    public void updateDetails(
            AlertType type,
            AlertCategory category,
            String title,
            String description,
            String location,
            Instant eventTime,
            List<String> photoUrls,
            String contactNumber
    ) {
        validateRequiredFields(this.reporterId, type, category, title, description);

        this.type = type;
        this.category = category;
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventTime = eventTime;
        this.photoUrls = photoUrls == null ? new ArrayList<>() : new ArrayList<>(photoUrls);
        this.contactNumber = normalizeContactNumber(contactNumber);
    }

    private static void validateRequiredFields(
            UUID reporterId,
            AlertType type,
            AlertCategory category,
            String title,
            String description
    ) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Title cannot be blank");
        }

        if (description == null || description.isBlank()) {
            throw new BusinessRuleException("Description cannot be blank");
        }

        if (reporterId == null || type == null || category == null) {
            throw new BusinessRuleException("Reporter ID, Type, and Category are required");
        }
    }

    private static String normalizeContactNumber(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.startsWith("00") && trimmed.length() > 2) {
            return "+" + trimmed.substring(2);
        }

        return trimmed;
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

    public AlertCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Instant getEventTime() {
        return eventTime;
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

    public AlertReportStatus getStatus() {
        return status;
    }

    public String getContactNumber() {
        return contactNumber;
    }
}
