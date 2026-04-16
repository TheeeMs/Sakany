package com.theMs.sakany.community.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository {
    Feedback save(Feedback feedback);
    Optional<Feedback> findById(UUID id);
    List<Feedback> findAllOrderByCreatedAtDesc();
    List<Feedback> findPublicFeedback();
    List<Feedback> findByAuthorId(UUID authorId);
    boolean hasVoted(UUID feedbackId, UUID voterId);
    void recordVote(UUID feedbackId, UUID voterId, VoteType voteType);
    void deleteById(UUID id);
}
