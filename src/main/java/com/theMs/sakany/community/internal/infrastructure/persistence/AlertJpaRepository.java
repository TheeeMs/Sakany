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

        @Query(value = """
            SELECT *
            FROM alerts a
            WHERE COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) <> 'RESOLVED'
            ORDER BY a.created_at DESC
            """,
            nativeQuery = true)
        List<AlertEntity> findByIsResolvedFalseOrderByCreatedAtDesc();

        @Query(value = """
                        SELECT
                                a.id AS reportId,
                                a.reporter_id AS reporterId,
                                a.type AS type,
                                a.category AS category,
                                COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) AS status,
                                a.title AS title,
                                a.description AS description,
                                a.location AS location,
                                a.event_time AS eventTime,
                                a.is_resolved AS resolved,
                                a.resolved_at AS resolvedAt,
                                a.created_at AS createdAt,
                                a.updated_at AS updatedAt,
                                u.first_name AS reporterFirstName,
                                u.last_name AS reporterLastName,
                                un.unit_number AS reporterUnitNumber,
                                b.name AS reporterBuildingName
                        FROM alerts a
                        LEFT JOIN users u ON u.id = a.reporter_id
                        LEFT JOIN resident_profiles rp ON rp.user_id = a.reporter_id
                        LEFT JOIN units un ON un.id = rp.unit_id
                        LEFT JOIN buildings b ON b.id = un.building_id
                        WHERE a.type IN ('MISSING', 'FOUND')
                            AND (
                                        :searchTerm IS NULL
                                        OR LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(a.location, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                    )
                            AND (
                                        :typeFilter IS NULL
                                        OR :typeFilter = 'ALL'
                                        OR a.type = :typeFilter
                                    )
                            AND (
                                        :statusFilter IS NULL
                                        OR :statusFilter = 'ALL'
                                        OR COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = :statusFilter
                                    )
                            AND (
                                        :category IS NULL
                                        OR a.category = :category
                                    )
                        ORDER BY COALESCE(a.event_time, a.created_at) DESC, a.created_at DESC
                        """,
                        countQuery = """
                        SELECT COUNT(*)
                        FROM alerts a
                        LEFT JOIN users u ON u.id = a.reporter_id
                        LEFT JOIN resident_profiles rp ON rp.user_id = a.reporter_id
                        LEFT JOIN units un ON un.id = rp.unit_id
                        LEFT JOIN buildings b ON b.id = un.building_id
                        WHERE a.type IN ('MISSING', 'FOUND')
                            AND (
                                        :searchTerm IS NULL
                                        OR LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(a.location, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                    )
                            AND (
                                        :typeFilter IS NULL
                                        OR :typeFilter = 'ALL'
                                        OR a.type = :typeFilter
                                    )
                            AND (
                                        :statusFilter IS NULL
                                        OR :statusFilter = 'ALL'
                                        OR COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) = :statusFilter
                                    )
                            AND (
                                        :category IS NULL
                                        OR a.category = :category
                                    )
                        """,
                        nativeQuery = true)
        Page<AdminMissingFoundRow> findMissingFoundReports(
                        @Param("searchTerm") String searchTerm,
                        @Param("typeFilter") String typeFilter,
                        @Param("statusFilter") String statusFilter,
                        @Param("category") String category,
                        Pageable pageable
        );

        @Query(value = """
                        SELECT
                                a.id AS reportId,
                                a.reporter_id AS reporterId,
                                a.type AS type,
                                a.category AS category,
                                COALESCE(a.status, CASE WHEN a.is_resolved THEN 'RESOLVED' ELSE 'OPEN' END) AS status,
                                a.title AS title,
                                a.description AS description,
                                a.location AS location,
                                a.event_time AS eventTime,
                                a.is_resolved AS resolved,
                                a.resolved_at AS resolvedAt,
                                a.created_at AS createdAt,
                                a.updated_at AS updatedAt,
                                u.first_name AS reporterFirstName,
                                u.last_name AS reporterLastName,
                                un.unit_number AS reporterUnitNumber,
                                b.name AS reporterBuildingName
                        FROM alerts a
                        LEFT JOIN users u ON u.id = a.reporter_id
                        LEFT JOIN resident_profiles rp ON rp.user_id = a.reporter_id
                        LEFT JOIN units un ON un.id = rp.unit_id
                        LEFT JOIN buildings b ON b.id = un.building_id
                        WHERE a.id = :reportId
                            AND a.type IN ('MISSING', 'FOUND')
                        """,
                        nativeQuery = true)
        Optional<AdminMissingFoundRow> findMissingFoundReportById(@Param("reportId") UUID reportId);

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
                        LEFT JOIN resident_profiles rp ON rp.user_id = a.reporter_id
                        LEFT JOIN units un ON un.id = rp.unit_id
                        LEFT JOIN buildings b ON b.id = un.building_id
                        WHERE a.type IN ('MISSING', 'FOUND')
                            AND (
                                        :searchTerm IS NULL
                                        OR LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(a.location, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                        OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                                    )
                            AND (
                                        :category IS NULL
                                        OR a.category = :category
                                    )
                        """,
                        nativeQuery = true)
        AdminMissingFoundSummaryRow getMissingFoundSummary(
                        @Param("searchTerm") String searchTerm,
                        @Param("category") String category
        );

        @Query(value = """
                        SELECT DISTINCT
                                a.category AS category
                        FROM alerts a
                        WHERE a.type IN ('MISSING', 'FOUND')
                        ORDER BY a.category ASC
                        """,
                        nativeQuery = true)
        List<AdminMissingFoundCategoryOptionRow> findMissingFoundCategoryOptions();
}
