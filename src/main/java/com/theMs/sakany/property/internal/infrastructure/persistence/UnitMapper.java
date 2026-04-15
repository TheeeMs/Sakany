package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.Unit;
import com.theMs.sakany.property.internal.domain.UnitType;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

@Component
public class UnitMapper {

    public UnitEntity toEntity(Unit unit) {
        if (unit == null) {
            return null;
        }

        UnitEntity entity = new UnitEntity(
            unit.getBuildingId(),
            unit.getUnitNumber(),
            unit.getFloor(),
            unit.getType()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, unit.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on UnitEntity", e);
        }

        return entity;
    }

    public Unit toDomain(UnitEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<Unit> constructor = Unit.class.getDeclaredConstructor(
                UUID.class, UUID.class, String.class, int.class, UnitType.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                entity.getId(),
                entity.getBuildingId(),
                entity.getUnitNumber(),
                entity.getFloor(),
                entity.getType()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map UnitEntity to Unit domain model", e);
        }
    }
}
