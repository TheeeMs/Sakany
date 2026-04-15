package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.accounts.internal.domain.User;
import com.theMs.sakany.shared.jpa.BaseEntity;
import org.springframework.stereotype.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;
import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.accounts.internal.domain.LoginMethod;

@Component
public class UserMapper {

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        UserEntity entity = new UserEntity(
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getEmail(),
            user.getHashedPassword(),
            user.getRole(),
            user.isActive(),
            user.isPhoneVerified(),
            user.getLoginMethod()
        );

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, user.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID on UserEntity", e);
        }

        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            Constructor<User> constructor = User.class.getDeclaredConstructor(
                UUID.class, String.class, String.class, String.class, String.class,
                String.class, Role.class, boolean.class, boolean.class, LoginMethod.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isActive(),
                entity.isPhoneVerified(),
                entity.getAuthProvider()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map UserEntity to User domain model", e);
        }
    }
}
