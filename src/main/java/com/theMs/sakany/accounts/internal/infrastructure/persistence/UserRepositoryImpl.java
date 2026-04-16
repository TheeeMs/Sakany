package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.accounts.internal.domain.User;
import com.theMs.sakany.accounts.internal.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final ResidentProfileJpaRepository residentProfileJpaRepository;
    private final UserMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, ResidentProfileJpaRepository residentProfileJpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.residentProfileJpaRepository = residentProfileJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity existingEntity = jpaRepository.findById(user.getId()).orElse(null);
        UserEntity savedEntity;
        if (existingEntity != null) {
            // Update existing entity to avoid detached/duplicate session object issues
            existingEntity.setFirstName(user.getFirstName());
            existingEntity.setLastName(user.getLastName());
            existingEntity.setPhone(user.getPhoneNumber());
            existingEntity.setEmail(user.getEmail());
            existingEntity.setPasswordHash(user.getHashedPassword());
            existingEntity.setRole(user.getRole());
            existingEntity.setActive(user.isActive());
            existingEntity.setPhoneVerified(user.isPhoneVerified());
            existingEntity.setAuthProvider(user.getLoginMethod());
            savedEntity = jpaRepository.save(existingEntity);
        } else {
            // Create new
            UserEntity entity = Objects.requireNonNull(mapper.toEntity(user), "User entity cannot be null");
            savedEntity = jpaRepository.save(entity);
        }

        if (user.getResidentProfile() != null) {
            residentProfileJpaRepository.deleteByUserId(savedEntity.getId());
            ResidentProfileEntity profileEntity = new ResidentProfileEntity(
                savedEntity,
                user.getResidentProfile().getUnitId(),
                user.getResidentProfile().getMoveInDate(),
                user.getResidentProfile().getType()
            );
            try {
                java.lang.reflect.Field idField = com.theMs.sakany.shared.jpa.BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(profileEntity, user.getResidentProfile().getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            residentProfileJpaRepository.save(profileEntity);
        }

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id, "User id cannot be null");
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return jpaRepository.findByPhone(phoneNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
}
