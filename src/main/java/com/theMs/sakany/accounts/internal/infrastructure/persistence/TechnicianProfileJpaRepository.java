package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.accounts.internal.domain.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TechnicianProfileJpaRepository extends JpaRepository<TechnicianProfileEntity, UUID> {

    @Query("""
            SELECT tp
            FROM TechnicianProfileEntity tp
            JOIN FETCH tp.user u
            WHERE u.role = :role
            """)
    List<TechnicianProfileEntity> findAllByUserRole(Role role);

    @Query("""
            SELECT tp
            FROM TechnicianProfileEntity tp
            JOIN FETCH tp.user u
            WHERE u.role = :role
              AND tp.isAvailable = true
            """)
    List<TechnicianProfileEntity> findAvailableByUserRole(Role role);

    @Query("""
            SELECT tp
            FROM TechnicianProfileEntity tp
            JOIN FETCH tp.user u
            WHERE u.id IN :userIds
            """)
    List<TechnicianProfileEntity> findByUserIds(@Param("userIds") Collection<UUID> userIds);

    @Query("""
            SELECT tp
            FROM TechnicianProfileEntity tp
            JOIN FETCH tp.user u
            WHERE u.id = :userId
            ORDER BY tp.createdAt ASC
            """)
    List<TechnicianProfileEntity> findByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
            DELETE FROM TechnicianProfileEntity tp
            WHERE tp.user.id = :userId
            """)
    void deleteByUserId(@Param("userId") UUID userId);
}
