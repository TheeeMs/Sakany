package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Feedback;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public Feedback toDomain(FeedbackEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Feedback.reconstitute(
            entity.getId(),
            entity.getAuthorId(),
            entity.getTitle(),
            entity.getContent(),
            entity.getType(),
            entity.isPublic(),
            entity.getStatus(),
            entity.getUpvotes(),
            entity.getDownvotes(),
            entity.getCategory(),
            entity.getLocation(),
            entity.isAnonymous(),
            entity.getAdminResponse(),
            entity.getImageUrl(),
            entity.getViewCount(),
            entity.getCreatedAt()
        );
    }

    public FeedbackEntity toEntity(Feedback domain) {
        if (domain == null) {
            return null;
        }
        
        FeedbackEntity entity = new FeedbackEntity();
        try {
            java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, domain.getId());
            
            if (domain.getCreatedAt() != null) {
                java.lang.reflect.Field createdAtField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(entity, domain.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID or CreatedAt on FeedbackEntity", e);
        }
        
        entity.setAuthorId(domain.getAuthorId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setType(domain.getType());
        entity.setPublic(domain.isPublic());
        entity.setStatus(domain.getStatus());
        entity.setUpvotes(domain.getUpvotes());
        entity.setDownvotes(domain.getDownvotes());
        entity.setCategory(domain.getCategory());
        entity.setLocation(domain.getLocation());
        entity.setAnonymous(domain.isAnonymous());
        entity.setAdminResponse(domain.getAdminResponse());
        entity.setImageUrl(domain.getImageUrl());
        entity.setViewCount(domain.getViewCount());
        
        return entity;
    }
}
