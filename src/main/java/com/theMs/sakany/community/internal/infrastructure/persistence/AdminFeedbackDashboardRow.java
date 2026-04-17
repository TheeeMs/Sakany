package com.theMs.sakany.community.internal.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

public interface AdminFeedbackDashboardRow {
    UUID getFeedbackId();

    UUID getAuthorId();

    String getAuthorFirstName();

    String getAuthorLastName();

    String getUnitNumber();

    String getTitle();

    String getContent();

    String getType();

    Boolean getIsPublic();

    String getWorkflowStatus();

    Integer getUpvotes();

    Integer getDownvotes();

    String getCategory();

    String getLocation();

    Boolean getIsAnonymous();

    String getAdminResponse();

    String getImageUrl();

    Integer getViewCount();

    Instant getCreatedAt();
}
