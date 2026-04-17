package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidentProfileJpaRepository extends JpaRepository<ResidentProfileEntity, UUID> {
    void deleteByUserId(UUID userId);

    Optional<ResidentProfileEntity> findByUserId(UUID userId);

    boolean existsByNationalId(String nationalId);

    boolean existsByNationalIdAndUserIdNot(String nationalId, UUID userId);
}
