package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
	Optional<UserEntity> findByPhone(String phone);
	Optional<UserEntity> findByEmail(String email);
}
