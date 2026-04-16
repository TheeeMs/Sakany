package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminResidentDirectoryJpaRepository extends JpaRepository<ResidentProfileEntity, UUID> {

    @Query(value = """
            SELECT
                u.id AS residentId,
                rp.id AS profileId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.phone AS phoneNumber,
                u.email AS email,
                u.is_active AS active,
                u.is_phone_verified AS phoneVerified,
                COALESCE(rp.approval_status, 'PENDING') AS approvalStatus,
                rp.resident_type AS residentType,
                rp.move_in_date AS moveInDate,
                rp.unit_id AS unitId,
                un.unit_number AS unitNumber,
                b.id AS buildingId,
                b.name AS buildingName,
                COALESCE(SUM(CASE WHEN i.status IN ('PENDING', 'OVERDUE') THEN i.amount ELSE 0 END), 0) AS dueAmount,
                COALESCE(MAX(i.currency), 'EGP') AS currency,
                u.created_at AS createdAt
            FROM resident_profiles rp
            JOIN users u ON u.id = rp.user_id
            LEFT JOIN units un ON un.id = rp.unit_id
            LEFT JOIN buildings b ON b.id = un.building_id
            LEFT JOIN invoices i ON i.resident_id = u.id
            WHERE u.role = 'RESIDENT'
              AND (
                    :searchTerm IS NULL
                    OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (:buildingId IS NULL OR b.id = :buildingId)
              AND (:approvalStatus IS NULL OR COALESCE(rp.approval_status, 'PENDING') = :approvalStatus)
              AND (
                    :status IS NULL
                    OR (:status = 'INACTIVE' AND u.is_active = FALSE)
                    OR (:status = 'PENDING' AND u.is_active = TRUE AND COALESCE(rp.approval_status, 'PENDING') = 'PENDING')
                    OR (:status = 'ACTIVE' AND u.is_active = TRUE AND COALESCE(rp.approval_status, 'PENDING') <> 'PENDING')
                  )
            GROUP BY
                u.id,
                rp.id,
                u.first_name,
                u.last_name,
                u.phone,
                u.email,
                u.is_active,
                u.is_phone_verified,
                rp.approval_status,
                rp.resident_type,
                rp.move_in_date,
                rp.unit_id,
                un.unit_number,
                b.id,
                b.name,
                u.created_at
            ORDER BY u.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM resident_profiles rp
            JOIN users u ON u.id = rp.user_id
            LEFT JOIN units un ON un.id = rp.unit_id
            LEFT JOIN buildings b ON b.id = un.building_id
            WHERE u.role = 'RESIDENT'
              AND (
                    :searchTerm IS NULL
                    OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(b.name, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (:buildingId IS NULL OR b.id = :buildingId)
              AND (:approvalStatus IS NULL OR COALESCE(rp.approval_status, 'PENDING') = :approvalStatus)
              AND (
                    :status IS NULL
                    OR (:status = 'INACTIVE' AND u.is_active = FALSE)
                    OR (:status = 'PENDING' AND u.is_active = TRUE AND COALESCE(rp.approval_status, 'PENDING') = 'PENDING')
                    OR (:status = 'ACTIVE' AND u.is_active = TRUE AND COALESCE(rp.approval_status, 'PENDING') <> 'PENDING')
                  )
            """,
            nativeQuery = true)
    Page<AdminResidentDirectoryRow> findResidentsForAdmin(
            @Param("searchTerm") String searchTerm,
            @Param("buildingId") UUID buildingId,
            @Param("status") String status,
            @Param("approvalStatus") String approvalStatus,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                u.id AS residentId,
                rp.id AS profileId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.phone AS phoneNumber,
                u.email AS email,
                u.is_active AS active,
                u.is_phone_verified AS phoneVerified,
                COALESCE(rp.approval_status, 'PENDING') AS approvalStatus,
                rp.resident_type AS residentType,
                rp.move_in_date AS moveInDate,
                rp.unit_id AS unitId,
                un.unit_number AS unitNumber,
                b.id AS buildingId,
                b.name AS buildingName,
                COALESCE(SUM(CASE WHEN i.status IN ('PENDING', 'OVERDUE') THEN i.amount ELSE 0 END), 0) AS dueAmount,
                COALESCE(MAX(i.currency), 'EGP') AS currency,
                u.created_at AS createdAt
            FROM resident_profiles rp
            JOIN users u ON u.id = rp.user_id
            LEFT JOIN units un ON un.id = rp.unit_id
            LEFT JOIN buildings b ON b.id = un.building_id
            LEFT JOIN invoices i ON i.resident_id = u.id
            WHERE u.role = 'RESIDENT'
              AND u.id = :residentId
            GROUP BY
                u.id,
                rp.id,
                u.first_name,
                u.last_name,
                u.phone,
                u.email,
                u.is_active,
                u.is_phone_verified,
                rp.approval_status,
                rp.resident_type,
                rp.move_in_date,
                rp.unit_id,
                un.unit_number,
                b.id,
                b.name,
                u.created_at
            """,
            nativeQuery = true)
    Optional<AdminResidentDirectoryRow> findResidentForAdmin(@Param("residentId") UUID residentId);

    @Query(value = """
            SELECT DISTINCT
                b.id AS buildingId,
                b.name AS buildingName
            FROM resident_profiles rp
            JOIN users u ON u.id = rp.user_id
            JOIN units un ON un.id = rp.unit_id
            JOIN buildings b ON b.id = un.building_id
            WHERE u.role = 'RESIDENT'
            ORDER BY b.name ASC
            """,
            nativeQuery = true)
    List<AdminResidentBuildingOptionRow> findResidentBuildingOptions();
}
