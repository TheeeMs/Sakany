package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Feedback;
import org.springframework.stereotype.Component;


@Component
public class FeedbackMapper {

    public FeedbackEntity toEntity(Feedback domain) {
        if (domain == null) return null;
        FeedbackEntity entity = new FeedbackEntity();
        if (domain.getId() != null) {
            try { java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(entity, domain.getId()); } catch (Exception e) { throw new RuntimeException(e); }
        }
        entity.setAuthorId(domain.getAuthorId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setType(domain.getType());
        entity.setPublic(domain.isPublic());
        entity.setStatus(domain.getStatus());
        entity.setUpvotes(domain.getUpvotes());
        entity.setDownvotes(domain.getDownvotes());
        return entity;
    }

    public Feedback toDomain(FeedbackEntity entity) {
        if (entity == null) return null;
        return Feedback.reconstitute(
            entity.getId(),
            entity.getAuthorId(),
            entity.getTitle(),
            entity.getContent(),
            entity.getType(),
            entity.isPublic(),
            entity.getStatus(),
            entity.getUpvotes(),
            entity.getDownvotes()
        );
    }
}
