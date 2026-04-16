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
            entity.getLocationLabel(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getStatus(),
                entity.isPublic(),
                entity.getPhotoUrls() != null ? new ArrayList<>(entity.getPhotoUrls()) : new ArrayList<>(),
                entity.getResolvedAt(),
            entity.getResolutionNotes(),
            entity.getResolutionCost(),
                entity.getCreatedAt()
        );
    }

    public MaintenanceRequestEntity toEntity(MaintenanceRequest domain) {
        if (domain == null) {
            return null;
        }

        MaintenanceRequestEntity entity = new MaintenanceRequestEntity();
        
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
            throw new RuntimeException("Failed to set ID or CreatedAt on MaintenanceRequestEntity", e);
        }

        entity.setResidentId(domain.getResidentId());
        entity.setUnitId(domain.getUnitId());
        entity.setTechnicianId(domain.getTechnicianId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setLocationLabel(domain.getLocationLabel());
        entity.setCategory(domain.getCategory());
        entity.setPriority(domain.getPriority());
        entity.setStatus(domain.getStatus());
        entity.setPublic(domain.isPublic());
        entity.setPhotoUrls(domain.getPhotoUrls() != null ? new ArrayList<>(domain.getPhotoUrls()) : new ArrayList<>());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setResolutionNotes(domain.getResolutionNotes());
        entity.setResolutionCost(domain.getResolutionCost());

        return entity;
    }
}
