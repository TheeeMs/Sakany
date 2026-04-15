package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Announcement;
import org.springframework.stereotype.Component;


@Component
public class AnnouncementMapper {

    public AnnouncementEntity toEntity(Announcement domain) {
        if (domain == null) return null;
        AnnouncementEntity entity = new AnnouncementEntity();
        if (domain.getId() != null) {
            try { java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(entity, domain.getId()); } catch (Exception e) { throw new RuntimeException(e); }
        }
        entity.setAuthorId(domain.getAuthorId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setPriority(domain.getPriority());
        entity.setActive(domain.isActive());
        entity.setExpiresAt(domain.getExpiresAt());
        return entity;
    }

    public Announcement toDomain(AnnouncementEntity entity) {
        if (entity == null) return null;
        return Announcement.reconstitute(
            entity.getId(),
            entity.getAuthorId(),
            entity.getTitle(),
            entity.getContent(),
            entity.getPriority(),
            entity.isActive(),
            entity.getExpiresAt()
        );
    }
}
