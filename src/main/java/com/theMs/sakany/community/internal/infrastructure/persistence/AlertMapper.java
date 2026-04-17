package com.theMs.sakany.community.internal.infrastructure.persistence;

import com.theMs.sakany.community.internal.domain.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertEntity toEntity(Alert domain) {
        if (domain == null) {
            return null;
        }

        AlertEntity entity = new AlertEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
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
        entity.setStatus(domain.getStatus());
        entity.setContactNumber(domain.getContactNumber());

        return entity;
    }

    public Alert toDomain(AlertEntity entity) {
        if (entity == null) {
            return null;
        }

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
                entity.getResolvedAt(),
                entity.getStatus(),
                entity.getContactNumber()
        );
    }
}
