package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminProfileJpaRepository extends JpaRepository<AdminProfileEntity, UUID> {

    @Query("""
            SELECT ap
            FROM AdminProfileEntity ap
            JOIN FETCH ap.user u
            WHERE u.id IN :userIds
            """)
    List<AdminProfileEntity> findByUserIds(@Param("userIds") Collection<UUID> userIds);

    @Query("""
            SELECT ap
            FROM AdminProfileEntity ap
            JOIN FETCH ap.user u
            WHERE u.id = :userId
            ORDER BY ap.createdAt ASC
            """)
    List<AdminProfileEntity> findByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
            DELETE FROM AdminProfileEntity ap
            WHERE ap.user.id = :userId
            """)
    void deleteByUserId(@Param("userId") UUID userId);
}
