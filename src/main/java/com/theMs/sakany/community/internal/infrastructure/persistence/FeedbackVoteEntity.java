package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.VoteType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "feedback_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"feedback_id", "voter_id"})
})
public class FeedbackVoteEntity extends BaseEntity {

    @Column(name = "feedback_id", nullable = false)
    private UUID feedbackId;

    @Column(name = "voter_id", nullable = false)
    private UUID voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(UUID feedbackId) {
        this.feedbackId = feedbackId;
    }

    public UUID getVoterId() {
        return voterId;
    }

    public void setVoterId(UUID voterId) {
        this.voterId = voterId;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
}
