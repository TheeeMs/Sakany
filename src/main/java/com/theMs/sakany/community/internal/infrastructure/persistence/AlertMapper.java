package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Alert;
import org.springframework.stereotype.Component;


@Component
public class AlertMapper {

    public AlertEntity toEntity(Alert domain) {
        if (domain == null) return null;
        AlertEntity entity = new AlertEntity();
        // Since id is mapped in BaseEntity and doesn't have a setter, use Reflection
        if (domain.getId() != null) {
            try { java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(entity, domain.getId()); } catch (Exception e) { throw new RuntimeException(e); }
        }
        entity.setReporterId(domain.getReporterId());
        entity.setType(domain.getType());
        entity.setCategory(domain.getCategory());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setLocation(domain.getLocation());
        entity.setEventTime(domain.getEventTime());
        entity.setPhotoUrls(domain.getPhotoUrls());
        entity.setResolved(domain.isResolved());
        entity.setResolvedAt(domain.getResolvedAt());
        return entity;
    }

    public Alert toDomain(AlertEntity entity) {
        if (entity == null) return null;
        return Alert.reconstitute(
            entity.getId(),
            entity.getReporterId(),
            entity.getType(),
            entity.getCategory(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getLocation(),
            entity.getEventTime(),
            entity.getPhotoUrls(),
            entity.isResolved(),
            entity.getResolvedAt()
        );
    }
}
