package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceRequest;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class MaintenanceRequestMapper {
    public MaintenanceRequest toDomain(MaintenanceRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return MaintenanceRequest.reconstitute(
                entity.getId(),
                entity.getResidentId(),
                entity.getUnitId(),
                entity.getTechnicianId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getStatus(),
                entity.isPublic(),
                entity.getPhotoUrls() != null ? new ArrayList<>(entity.getPhotoUrls()) : new ArrayList<>(),
                entity.getResolvedAt()
        );
    }

    public MaintenanceRequestEntity toEntity(MaintenanceRequest domain) {
        if (domain == null) {
            return null;
        }

        MaintenanceRequestEntity entity = new MaintenanceRequestEntity();
        
        // Since we don't have an explicit setId in BaseEntity we need to handle reflection or use reflection inside BaseEntity to set it.
        // Assuming BaseEntity has an ID we need to find a way to set it. We can do it using reflection.
        try {
            var idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, domain.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID", e);
        }

        entity.setResidentId(domain.getResidentId());
        entity.setUnitId(domain.getUnitId());
        entity.setTechnicianId(domain.getTechnicianId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setPriority(domain.getPriority());
        entity.setStatus(domain.getStatus());
        entity.setPublic(domain.isPublic());
        entity.setPhotoUrls(domain.getPhotoUrls() != null ? new ArrayList<>(domain.getPhotoUrls()) : new ArrayList<>());
        entity.setResolvedAt(domain.getResolvedAt());

        return entity;
    }
}
