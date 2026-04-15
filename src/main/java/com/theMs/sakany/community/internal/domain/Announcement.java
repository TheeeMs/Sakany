package com.theMs.sakany.community.internal.domain;

import com.theMs.sakany.community.internal.domain.events.AnnouncementDeactivated;
import com.theMs.sakany.community.internal.domain.events.AnnouncementPublished;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class Announcement extends AggregateRoot {
    private UUID id;
    private UUID authorId;
    private String title;
    private String content;
    private AnnouncementPriority priority;
    private boolean isActive;
    private Instant expiresAt;

    private Announcement(UUID id, UUID authorId, String title, String content, AnnouncementPriority priority, boolean isActive, Instant expiresAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.isActive = isActive;
        this.expiresAt = expiresAt;
    }

    public static Announcement create(UUID authorId, String title, String content, AnnouncementPriority priority, Instant expiresAt) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Title cannot be empty");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessRuleException("Content cannot be empty");
        }
        if (authorId == null || priority == null) {
            throw new BusinessRuleException("Author ID and Priority are required");
        }

        UUID id = UUID.randomUUID();
        Announcement announcement = new Announcement(id, authorId, title, content, priority, true, expiresAt);
        announcement.registerEvent(new AnnouncementPublished(id, authorId, title, content, priority, expiresAt, Instant.now()));
        return announcement;
    }

    public static Announcement reconstitute(UUID id, UUID authorId, String title, String content, AnnouncementPriority priority, boolean isActive, Instant expiresAt) {
        return new Announcement(id, authorId, title, content, priority, isActive, expiresAt);
    }

    public void deactivate() {
        if (!this.isActive) {
            throw new BusinessRuleException("Announcement is already inactive");
        }
        this.isActive = false;
        this.registerEvent(new AnnouncementDeactivated(this.id, Instant.now()));
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public AnnouncementPriority getPriority() {
        return priority;
    }

    public boolean isActive() {
        return isActive;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
