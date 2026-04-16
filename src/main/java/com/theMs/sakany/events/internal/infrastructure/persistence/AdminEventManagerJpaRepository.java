package com.theMs.sakany.events.internal.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminEventManagerJpaRepository extends JpaRepository<CommunityEventEntity, UUID> {

    @Query(value = """
            SELECT
                ce.id AS eventId,
                ce.title AS title,
                ce.description AS description,
                ce.location AS location,
                ce.start_date AS startDate,
                ce.end_date AS endDate,
                ce.image_url AS imageUrl,
                ce.category AS category,
                ce.status AS workflowStatus,
                CASE
                    WHEN ce.status = 'PROPOSED' THEN 'PENDING'
                    WHEN ce.status = 'REJECTED' THEN 'REJECTED'
                    WHEN ce.status IN ('COMPLETED', 'CANCELLED') THEN 'COMPLETED'
                    WHEN ce.status = 'APPROVED' AND ce.start_date <= NOW() AND (ce.end_date IS NULL OR ce.end_date >= NOW()) THEN 'ONGOING'
                    WHEN ce.status = 'APPROVED' AND ce.end_date IS NOT NULL AND ce.end_date < NOW() THEN 'COMPLETED'
                    WHEN ce.status = 'APPROVED' THEN 'APPROVED'
                    ELSE ce.status
                END AS uiStatus,
                ce.organizer_id AS organizerId,
                u.first_name AS organizerFirstName,
                u.last_name AS organizerLastName,
                ce.host_name AS hostName,
                ce.current_attendees AS currentAttendees,
                ce.max_attendees AS maxAttendees,
                ce.created_at AS createdAt
            FROM community_events ce
            LEFT JOIN users u ON u.id = ce.organizer_id
            WHERE (
                    :searchTerm IS NULL
                    OR LOWER(ce.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(ce.host_name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(ce.location, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    :category IS NULL
                    OR LOWER(COALESCE(ce.category, '')) = LOWER(:category)
                  )
              AND (
                    :statusFilter IS NULL
                    OR :statusFilter = 'ALL'
                    OR (:statusFilter = 'PENDING' AND ce.status = 'PROPOSED')
                    OR (:statusFilter = 'REJECTED' AND ce.status = 'REJECTED')
                    OR (:statusFilter = 'APPROVED' AND ce.status = 'APPROVED' AND ce.start_date > NOW())
                    OR (:statusFilter = 'ONGOING' AND ce.status = 'APPROVED' AND ce.start_date <= NOW() AND (ce.end_date IS NULL OR ce.end_date >= NOW()))
                    OR (:statusFilter = 'COMPLETED' AND (ce.status IN ('COMPLETED', 'CANCELLED') OR (ce.status = 'APPROVED' AND ce.end_date IS NOT NULL AND ce.end_date < NOW())))
                  )
            ORDER BY ce.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM community_events ce
            LEFT JOIN users u ON u.id = ce.organizer_id
            WHERE (
                    :searchTerm IS NULL
                    OR LOWER(ce.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(ce.host_name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(ce.location, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    :category IS NULL
                    OR LOWER(COALESCE(ce.category, '')) = LOWER(:category)
                  )
              AND (
                    :statusFilter IS NULL
                    OR :statusFilter = 'ALL'
                    OR (:statusFilter = 'PENDING' AND ce.status = 'PROPOSED')
                    OR (:statusFilter = 'REJECTED' AND ce.status = 'REJECTED')
                    OR (:statusFilter = 'APPROVED' AND ce.status = 'APPROVED' AND ce.start_date > NOW())
                    OR (:statusFilter = 'ONGOING' AND ce.status = 'APPROVED' AND ce.start_date <= NOW() AND (ce.end_date IS NULL OR ce.end_date >= NOW()))
                    OR (:statusFilter = 'COMPLETED' AND (ce.status IN ('COMPLETED', 'CANCELLED') OR (ce.status = 'APPROVED' AND ce.end_date IS NOT NULL AND ce.end_date < NOW())))
                  )
            """,
            nativeQuery = true)
    Page<AdminEventManagerRow> findForDashboard(
            @Param("searchTerm") String searchTerm,
            @Param("statusFilter") String statusFilter,
            @Param("category") String category,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                COUNT(*) AS totalCount,
                COALESCE(SUM(CASE WHEN ce.status = 'PROPOSED' THEN 1 ELSE 0 END), 0) AS pendingCount,
                COALESCE(SUM(CASE WHEN ce.status = 'APPROVED' AND ce.start_date > NOW() THEN 1 ELSE 0 END), 0) AS approvedCount,
                COALESCE(SUM(CASE WHEN ce.status = 'APPROVED' AND ce.start_date <= NOW() AND (ce.end_date IS NULL OR ce.end_date >= NOW()) THEN 1 ELSE 0 END), 0) AS ongoingCount,
                COALESCE(SUM(CASE WHEN ce.status IN ('COMPLETED', 'CANCELLED') OR (ce.status = 'APPROVED' AND ce.end_date IS NOT NULL AND ce.end_date < NOW()) THEN 1 ELSE 0 END), 0) AS completedCount,
                COALESCE(SUM(CASE WHEN ce.status = 'REJECTED' THEN 1 ELSE 0 END), 0) AS rejectedCount
            FROM community_events ce
            """,
            nativeQuery = true)
    AdminEventSummaryRow getDashboardSummary();

    @Query(value = """
            SELECT
                ce.id AS eventId,
                ce.title AS title,
                ce.description AS description,
                ce.location AS location,
                ce.start_date AS startDate,
                ce.end_date AS endDate,
                ce.image_url AS imageUrl,
                ce.category AS category,
                ce.status AS workflowStatus,
                'PENDING' AS uiStatus,
                ce.organizer_id AS organizerId,
                u.first_name AS organizerFirstName,
                u.last_name AS organizerLastName,
                ce.host_name AS hostName,
                ce.current_attendees AS currentAttendees,
                ce.max_attendees AS maxAttendees,
                ce.created_at AS createdAt
            FROM community_events ce
            LEFT JOIN users u ON u.id = ce.organizer_id
            WHERE ce.status = 'PROPOSED'
            ORDER BY ce.created_at ASC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<AdminEventManagerRow> findTopPendingApproval();

    @Query(value = """
            SELECT DISTINCT ce.category AS category
            FROM community_events ce
            WHERE ce.category IS NOT NULL
              AND TRIM(ce.category) <> ''
            ORDER BY ce.category ASC
            """,
            nativeQuery = true)
    List<AdminEventCategoryOptionRow> findCategoryOptions();
}
