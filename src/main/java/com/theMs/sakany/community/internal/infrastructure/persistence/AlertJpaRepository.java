package com.theMs.sakany.community.internal.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findByIsResolvedFalseOrderByCreatedAtDesc();

    @Query(value = """
            SELECT
                a.id AS reportId,
                a.reporter_id AS reporterId,
                a.type AS reportType,
                a.category AS category,
                a.title AS title,
                a.description AS description,
                a.location AS location,
                a.event_time AS eventTime,
                COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) AS status,
                a.resolved_at AS resolvedAt,
                a.contact_number AS contactNumber,
                a.created_at AS createdAt,
                COALESCE(NULLIF(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), ''), 'Unknown Reporter') AS reporterName,
                CASE
                    WHEN b.name IS NULL AND un.unit_number IS NULL THEN 'N/A'
                    WHEN b.name IS NULL THEN un.unit_number
                    WHEN un.unit_number IS NULL THEN b.name
                    ELSE CONCAT(b.name, '-', un.unit_number)
                END AS reporterUnitLabel
            FROM alerts a
            LEFT JOIN users u ON u.id = a.reporter_id
            LEFT JOIN resident_profiles rp ON rp.user_id = a.reporter_id
            LEFT JOIN units un ON un.id = rp.unit_id
            LEFT JOIN buildings b ON b.id = un.building_id
            WHERE (
                    :searchTerm IS NULL
                    OR LOWER(COALESCE(a.title, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(a.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    ((:typeFilter IS NULL OR :typeFilter = 'ALL') AND a.type IN ('MISSING', 'FOUND'))
                    OR a.type = :typeFilter
                  )
              AND (
                    :statusFilter IS NULL
                    OR :statusFilter = 'ALL'
                    OR COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = :statusFilter
                  )
              AND (:categoryFilter IS NULL OR a.category = :categoryFilter)
            ORDER BY COALESCE(a.event_time, a.created_at) DESC, a.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM alerts a
            LEFT JOIN users u ON u.id = a.reporter_id
            WHERE (
                    :searchTerm IS NULL
                    OR LOWER(COALESCE(a.title, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(a.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    ((:typeFilter IS NULL OR :typeFilter = 'ALL') AND a.type IN ('MISSING', 'FOUND'))
                    OR a.type = :typeFilter
                  )
              AND (
                    :statusFilter IS NULL
                    OR :statusFilter = 'ALL'
                    OR COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = :statusFilter
                  )
              AND (:categoryFilter IS NULL OR a.category = :categoryFilter)
            """,
            nativeQuery = true)
    Page<AdminMissingFoundReportRow> findMissingFoundReportsForAdmin(
            @Param("searchTerm") String searchTerm,
            @Param("typeFilter") String typeFilter,
            @Param("statusFilter") String statusFilter,
            @Param("categoryFilter") String categoryFilter,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                COUNT(*) AS totalCount,
                COALESCE(SUM(CASE WHEN a.type = 'MISSING' THEN 1 ELSE 0 END), 0) AS missingCount,
                COALESCE(SUM(CASE WHEN a.type = 'FOUND' THEN 1 ELSE 0 END), 0) AS foundCount,
                COALESCE(SUM(CASE WHEN COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = 'OPEN' THEN 1 ELSE 0 END), 0) AS openCount,
                COALESCE(SUM(CASE WHEN COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = 'MATCHED' THEN 1 ELSE 0 END), 0) AS matchedCount,
                COALESCE(SUM(CASE WHEN COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = 'RESOLVED' THEN 1 ELSE 0 END), 0) AS resolvedCount
            FROM alerts a
            LEFT JOIN users u ON u.id = a.reporter_id
            WHERE (
                    :searchTerm IS NULL
                    OR LOWER(COALESCE(a.title, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(a.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    ((:typeFilter IS NULL OR :typeFilter = 'ALL') AND a.type IN ('MISSING', 'FOUND'))
                    OR a.type = :typeFilter
                  )
              AND (
                    :statusFilter IS NULL
                    OR :statusFilter = 'ALL'
                    OR COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = :statusFilter
                  )
              AND (:categoryFilter IS NULL OR a.category = :categoryFilter)
            """,
            nativeQuery = true)
    AdminMissingFoundSummaryRow getMissingFoundSummaryForAdmin(
            @Param("searchTerm") String searchTerm,
            @Param("typeFilter") String typeFilter,
            @Param("statusFilter") String statusFilter,
            @Param("categoryFilter") String categoryFilter
    );

    @Query(value = """
            SELECT
                a.id AS reportId,
                a.reporter_id AS reporterId,
                a.type AS reportType,
                a.category AS category,
                a.title AS title,
                a.description AS description,
                a.location AS location,
                a.event_time AS eventTime,
                COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) AS status,
                a.resolved_at AS resolvedAt,
                a.contact_number AS contactNumber,
                a.created_at AS createdAt,
                COALESCE(NULLIF(TRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))), ''), 'Unknown Reporter') AS reporterName,
                CASE
                    WHEN b.name IS NULL AND un.unit_number IS NULL THEN 'N/A'
                    WHEN b.name IS NULL THEN un.unit_number
                    WHEN un.unit_number IS NULL THEN b.name
                    ELSE CONCAT(b.name, '-', un.unit_number)
                END AS reporterUnitLabel
            FROM alerts a
            LEFT JOIN users u ON u.id = a.reporter_id
            LEFT JOIN resident_profiles rp ON rp.user_id = a.reporter_id
            LEFT JOIN units un ON un.id = rp.unit_id
            LEFT JOIN buildings b ON b.id = un.building_id
            WHERE a.id = :reportId
              AND a.type IN ('MISSING', 'FOUND')
            """,
            nativeQuery = true)
    Optional<AdminMissingFoundReportRow> findMissingFoundReportForAdmin(@Param("reportId") UUID reportId);

    @Query(value = """
            SELECT DISTINCT a.category
            FROM alerts a
            WHERE a.type IN ('MISSING', 'FOUND')
            ORDER BY a.category ASC
            """,
            nativeQuery = true)
    List<String> findMissingFoundCategoriesForAdmin();
}
