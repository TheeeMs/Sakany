package com.theMs.sakany.community.internal.domain;

import com.theMs.sakany.community.internal.domain.events.FeedbackStatusChanged;
import com.theMs.sakany.community.internal.domain.events.FeedbackSubmitted;
import com.theMs.sakany.community.internal.domain.events.FeedbackVoted;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class Feedback extends AggregateRoot {
    private UUID id;
    private UUID authorId;
    private String title;
    private String content;
    private FeedbackType type;
    private boolean isPublic;
    private FeedbackStatus status;
    private int upvotes;
    private int downvotes;

    // Added fields for UI parity
    private String category;
    private String location;
    private boolean isAnonymous;

    // Added fields for specific UI components
    private String adminResponse;
    private String imageUrl;
    private int viewCount;
    private Instant createdAt;

    private Feedback(UUID id, UUID authorId, String title, String content, FeedbackType type, boolean isPublic, FeedbackStatus status, int upvotes, int downvotes, String category, String location, boolean isAnonymous, String adminResponse, String imageUrl, int viewCount, Instant createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isPublic = isPublic;
        this.status = status;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        
        this.category = category;
        this.location = location;
        this.isAnonymous = isAnonymous;
        this.adminResponse = adminResponse;
        this.imageUrl = imageUrl;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }

    public static Feedback create(UUID authorId, String title, String content, FeedbackType type, boolean isPublic, String category, String location, boolean isAnonymous, String imageUrl) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Title cannot be empty");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessRuleException("Content cannot be empty");
        }
        if (authorId == null || type == null) {
            throw new BusinessRuleException("Author ID and Type are required");
        }

        UUID id = UUID.randomUUID();
        Feedback feedback = new Feedback(id, authorId, title, content, type, isPublic, FeedbackStatus.UNDER_REVIEW, 0, 0, category, location, isAnonymous, null, imageUrl, 0, Instant.now());
        feedback.registerEvent(new FeedbackSubmitted(id, authorId, title, content, type, isPublic, Instant.now()));
        return feedback;
    }

    public static Feedback reconstitute(UUID id, UUID authorId, String title, String content, FeedbackType type, boolean isPublic, FeedbackStatus status, int upvotes, int downvotes, String category, String location, boolean isAnonymous, String adminResponse, String imageUrl, int viewCount, Instant createdAt) {
        return new Feedback(id, authorId, title, content, type, isPublic, status, upvotes, downvotes, category, location, isAnonymous, adminResponse, imageUrl, viewCount, createdAt);
    }

    public void upvote(UUID voterId) {
        if (!isPublic) {
            throw new BusinessRuleException("Cannot vote on private feedback");
        }
        this.upvotes++;
        this.registerEvent(new FeedbackVoted(this.id, voterId, VoteType.UP, Instant.now()));
    }

    public void downvote(UUID voterId) {
        if (!isPublic) {
            throw new BusinessRuleException("Cannot vote on private feedback");
        }
        this.downvotes++;
        this.registerEvent(new FeedbackVoted(this.id, voterId, VoteType.DOWN, Instant.now()));
    }

    public void updateStatus(FeedbackStatus newStatus) {
        if (newStatus == null) {
            throw new BusinessRuleException("Status cannot be null");
        }
        if (this.status != newStatus) {
            FeedbackStatus oldStatus = this.status;
            this.status = newStatus;
            this.registerEvent(new FeedbackStatusChanged(this.id, oldStatus, newStatus, Instant.now()));
        }
    }

    public void close() {
        this.updateStatus(FeedbackStatus.CLOSED);
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

    public FeedbackType getType() {
        return type;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public FeedbackStatus getStatus() {
        return status;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getViewCount() {
        return viewCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
