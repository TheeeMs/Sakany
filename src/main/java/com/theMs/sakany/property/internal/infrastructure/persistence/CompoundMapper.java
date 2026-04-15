package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.property.internal.domain.Compound;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

@Component
public class CompoundMapper {

    public CompoundEntity toEntity(Compound compound) {
        if (compound == null) {
            return null;
        }

        CompoundEntity entity = new CompoundEntity(
            compound.getName(),
            compound.getAddress()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, compound.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on CompoundEntity", e);
        }

        return entity;
    }

    public Compound toDomain(CompoundEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<Compound> constructor = Compound.class.getDeclaredConstructor(
                UUID.class, String.class, String.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                entity.getId(),
                entity.getName(),
                entity.getAddress()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map CompoundEntity to Compound domain model", e);
        }
    }
}
