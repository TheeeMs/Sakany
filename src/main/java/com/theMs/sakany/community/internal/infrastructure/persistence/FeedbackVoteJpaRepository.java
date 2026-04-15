package com.theMs.sakany.community.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeedbackVoteJpaRepository extends JpaRepository<FeedbackVoteEntity, UUID> {
    boolean existsByFeedbackIdAndVoterId(UUID feedbackId, UUID voterId);
}
