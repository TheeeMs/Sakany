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
    private final UserMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity existingEntity = jpaRepository.findById(user.getId()).orElse(null);
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
            return mapper.toDomain(jpaRepository.save(existingEntity));
        } else {
            // Create new
            UserEntity entity = Objects.requireNonNull(mapper.toEntity(user), "User entity cannot be null");
            UserEntity savedEntity = jpaRepository.save(entity);
            return mapper.toDomain(savedEntity);
        }
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
