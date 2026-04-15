package com.theMs.sakany.community.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackJpaRepository extends JpaRepository<FeedbackEntity, UUID> {
    List<FeedbackEntity> findByIsPublicTrueOrderByCreatedAtDesc();
}
