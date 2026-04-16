package com.theMs.sakany.events.internal.infrastructure.persistence;

import com.theMs.sakany.events.internal.domain.CommunityEvent;
import org.springframework.stereotype.Component;

@Component
public class CommunityEventMapper {

    public CommunityEvent toDomain(CommunityEventEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CommunityEvent(
                entity.getId(),
                entity.getOrganizerId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getImageUrl(),
                entity.getHostName(),
                entity.getPrice(),
                entity.getMaxAttendees(),
                entity.getCategory(),
                entity.getHostRole(),
                entity.getContactPhone(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCurrentAttendees(),
                entity.getStatus(),
                entity.getApprovedBy()
        );
    }

    public CommunityEventEntity toEntity(CommunityEvent domain) {
        if (domain == null) {
            return null;
        }
        return new CommunityEventEntity(
                domain.getId(),
                domain.getOrganizerId(),
                domain.getTitle(),
                domain.getDescription(),
                domain.getLocation(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getImageUrl(),
                domain.getHostName(),
                domain.getPrice(),
                domain.getMaxAttendees(),
                domain.getCategory(),
                domain.getHostRole(),
                domain.getContactPhone(),
                domain.getLatitude(),
                domain.getLongitude(),
                domain.getCurrentAttendees(),
                domain.getStatus(),
                domain.getApprovedBy()
        );
    }
}
