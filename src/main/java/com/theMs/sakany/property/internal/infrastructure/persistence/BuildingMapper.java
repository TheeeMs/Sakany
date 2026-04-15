package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.Building;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

@Component
public class BuildingMapper {

    public BuildingEntity toEntity(Building building) {
        if (building == null) {
            return null;
        }

        BuildingEntity entity = new BuildingEntity(
            building.getCompoundId(),
            building.getName(),
            building.getNumberOfFloors()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, building.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on BuildingEntity", e);
        }

        return entity;
    }

    public Building toDomain(BuildingEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<Building> constructor = Building.class.getDeclaredConstructor(
                UUID.class, UUID.class, String.class, int.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                entity.getId(),
                entity.getCompoundId(),
                entity.getName(),
                entity.getNumberOfFloors()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map BuildingEntity to Building domain model", e);
        }
    }
}
