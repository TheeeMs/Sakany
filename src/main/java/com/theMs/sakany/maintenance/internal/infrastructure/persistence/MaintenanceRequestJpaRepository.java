package com.theMs.sakany.maintenance.internal.infrastructure.persistence;

import com.theMs.sakany.maintenance.internal.domain.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRequestJpaRepository extends JpaRepository<MaintenanceRequestEntity, UUID> {
    List<MaintenanceRequestEntity> findByResidentId(UUID residentId);
    List<MaintenanceRequestEntity> findByStatus(MaintenanceStatus status);

    @Query(value = """
            SELECT
                mr.id AS requestId,
                mr.is_public AS publicRequest,
                mr.priority AS priority,
                mr.category AS category,
                mr.title AS issueTitle,
                COALESCE(
                    mr.location_label,
                    CASE
                        WHEN un.unit_number IS NOT NULL THEN CONCAT('Unit ', un.unit_number)
                        WHEN b.name IS NOT NULL THEN b.name
                        ELSE 'N/A'
                    END
                ) AS locationLabel,
                un.unit_number AS unitNumber,
                b.name AS buildingName,
                mr.created_at AS requestedAt,
                mr.status AS workflowStatus,
                mr.technician_id AS technicianId
            FROM maintenance_requests mr
            LEFT JOIN units un ON mr.unit_id = un.id
            LEFT JOIN buildings b ON un.building_id = b.id
            WHERE (
                    :area IS NULL
                    OR LOWER(COALESCE(mr.location_label, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                    OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                  )
              AND (
                    :requestType IS NULL
                    OR (:requestType = 'PUBLIC' AND mr.is_public = TRUE)
                    OR (:requestType = 'PRIVATE' AND mr.is_public = FALSE)
                  )
              AND (:category IS NULL OR mr.category = :category)
              AND (
                    :tab IS NULL
                    OR :tab = 'ALL'
                    OR (:tab = 'PENDING' AND mr.status IN ('SUBMITTED', 'ASSIGNED'))
                    OR (:tab = 'IN_PROGRESS' AND mr.status = 'IN_PROGRESS')
                    OR (:tab = 'COMPLETED' AND mr.status = 'RESOLVED')
                  )
            ORDER BY
                CASE WHEN :sortBy = 'OLDEST' THEN mr.created_at END ASC,
                CASE WHEN :sortBy = 'NEWEST' OR :sortBy IS NULL THEN mr.created_at END DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM maintenance_requests mr
            LEFT JOIN units un ON mr.unit_id = un.id
            LEFT JOIN buildings b ON un.building_id = b.id
            WHERE (
                    :area IS NULL
                    OR LOWER(COALESCE(mr.location_label, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                    OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                  )
              AND (
                    :requestType IS NULL
                    OR (:requestType = 'PUBLIC' AND mr.is_public = TRUE)
                    OR (:requestType = 'PRIVATE' AND mr.is_public = FALSE)
                  )
              AND (:category IS NULL OR mr.category = :category)
              AND (
                    :tab IS NULL
                    OR :tab = 'ALL'
                    OR (:tab = 'PENDING' AND mr.status IN ('SUBMITTED', 'ASSIGNED'))
                    OR (:tab = 'IN_PROGRESS' AND mr.status = 'IN_PROGRESS')
                    OR (:tab = 'COMPLETED' AND mr.status = 'RESOLVED')
                  )
            """,
            nativeQuery = true)
    Page<AdminMaintenanceCommandCenterRow> findForCommandCenter(
            @Param("tab") String tab,
            @Param("area") String area,
            @Param("requestType") String requestType,
            @Param("category") String category,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                COUNT(*) AS totalCount,
                SUM(CASE WHEN mr.status IN ('SUBMITTED', 'ASSIGNED') THEN 1 ELSE 0 END) AS pendingCount,
                SUM(CASE WHEN mr.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS inProgressCount,
                SUM(CASE WHEN mr.status = 'RESOLVED' THEN 1 ELSE 0 END) AS completedCount
            FROM maintenance_requests mr
            LEFT JOIN units un ON mr.unit_id = un.id
            LEFT JOIN buildings b ON un.building_id = b.id
            WHERE (
                    :area IS NULL
                    OR LOWER(COALESCE(mr.location_label, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                    OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :area, '%'))
                  )
              AND (
                    :requestType IS NULL
                    OR (:requestType = 'PUBLIC' AND mr.is_public = TRUE)
                    OR (:requestType = 'PRIVATE' AND mr.is_public = FALSE)
                  )
              AND (:category IS NULL OR mr.category = :category)
            """,
            nativeQuery = true)
    AdminMaintenanceCommandCenterSummaryRow getCommandCenterSummary(
            @Param("area") String area,
            @Param("requestType") String requestType,
            @Param("category") String category
    );

    @Query(value = """
            SELECT DISTINCT
                COALESCE(
                    mr.location_label,
                    CASE
                        WHEN un.unit_number IS NOT NULL THEN CONCAT('Unit ', un.unit_number)
                        WHEN b.name IS NOT NULL THEN b.name
                        ELSE 'N/A'
                    END
                ) AS areaLabel
            FROM maintenance_requests mr
            LEFT JOIN units un ON mr.unit_id = un.id
            LEFT JOIN buildings b ON un.building_id = b.id
            ORDER BY areaLabel ASC
            """,
            nativeQuery = true)
    List<AdminMaintenanceAreaOptionRow> findDistinctAreaOptions();
}
