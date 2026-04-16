package com.theMs.sakany.access.internal.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AdminQrAccessDirectoryJpaRepository extends JpaRepository<AccessCodeEntity, UUID> {

    @Query(value = """
            SELECT
                u.id AS residentId,
                u.first_name AS firstName,
                u.last_name AS lastName,
                u.phone AS phoneNumber,
                un.unit_number AS unitNumber,
                b.name AS buildingName
            FROM users u
            LEFT JOIN resident_profiles rp ON rp.user_id = u.id
            LEFT JOIN units un ON un.id = rp.unit_id
            LEFT JOIN buildings b ON b.id = un.building_id
            WHERE u.id = :residentId
              AND u.role = 'RESIDENT'
            """,
            nativeQuery = true)
    Optional<AdminQrResidentHeaderRow> findResidentHeaderForAdmin(@Param("residentId") UUID residentId);

    @Query(value = """
            SELECT
                ac.id AS accessCodeId,
                ac.code AS accessCode,
                ac.resident_id AS residentId,
                u.first_name AS residentFirstName,
                u.last_name AS residentLastName,
                ac.visitor_name AS visitorName,
                ac.visitor_phone AS visitorPhone,
                ac.purpose AS purpose,
                un.unit_number AS unitNumber,
                ac.created_at AS createdAt,
                ac.valid_until AS validUntil,
                CASE
                    WHEN ac.status = 'ACTIVE' AND ac.valid_until < NOW() THEN 'EXPIRED'
                    ELSE ac.status
                END AS effectiveStatus
            FROM access_codes ac
            JOIN users u ON u.id = ac.resident_id
            LEFT JOIN resident_profiles rp ON rp.user_id = u.id
            LEFT JOIN units un ON un.id = rp.unit_id
            WHERE u.role = 'RESIDENT'
              AND (
                    :searchTerm IS NULL
                    OR LOWER(ac.visitor_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(ac.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    :status IS NULL
                    OR (:status = 'ACTIVE' AND ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW())
                    OR (:status = 'USED' AND ac.status = 'USED')
                    OR (:status = 'REVOKED' AND ac.status = 'REVOKED')
                    OR (:status = 'EXPIRED' AND (ac.status = 'EXPIRED' OR (ac.status = 'ACTIVE' AND ac.valid_until < NOW())))
                  )
              AND (:purpose IS NULL OR ac.purpose = :purpose)
            ORDER BY ac.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM access_codes ac
            JOIN users u ON u.id = ac.resident_id
            LEFT JOIN resident_profiles rp ON rp.user_id = u.id
            LEFT JOIN units un ON un.id = rp.unit_id
            WHERE u.role = 'RESIDENT'
              AND (
                    :searchTerm IS NULL
                    OR LOWER(ac.visitor_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(ac.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    :status IS NULL
                    OR (:status = 'ACTIVE' AND ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW())
                    OR (:status = 'USED' AND ac.status = 'USED')
                    OR (:status = 'REVOKED' AND ac.status = 'REVOKED')
                    OR (:status = 'EXPIRED' AND (ac.status = 'EXPIRED' OR (ac.status = 'ACTIVE' AND ac.valid_until < NOW())))
                  )
              AND (:purpose IS NULL OR ac.purpose = :purpose)
            """,
            nativeQuery = true)
    Page<AdminQrAccessRow> findForAdmin(
            @Param("searchTerm") String searchTerm,
            @Param("status") String status,
            @Param("purpose") String purpose,
            Pageable pageable
    );

    @Query(value = """
        SELECT
          ac.id AS accessCodeId,
          ac.code AS accessCode,
          ac.resident_id AS residentId,
          u.first_name AS residentFirstName,
          u.last_name AS residentLastName,
          ac.visitor_name AS visitorName,
          ac.visitor_phone AS visitorPhone,
          ac.purpose AS purpose,
          un.unit_number AS unitNumber,
          ac.created_at AS createdAt,
          ac.valid_until AS validUntil,
          CASE
            WHEN ac.status = 'ACTIVE' AND ac.valid_until < NOW() THEN 'EXPIRED'
            ELSE ac.status
          END AS effectiveStatus
        FROM access_codes ac
        JOIN users u ON u.id = ac.resident_id
        LEFT JOIN resident_profiles rp ON rp.user_id = u.id
        LEFT JOIN units un ON un.id = rp.unit_id
        WHERE ac.id = :accessCodeId
          AND u.role = 'RESIDENT'
        """,
        nativeQuery = true)
    Optional<AdminQrAccessRow> findCodeByIdForAdmin(@Param("accessCodeId") UUID accessCodeId);

    @Query(value = """
            SELECT
                COUNT(*) AS totalCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'GUEST' THEN 1 ELSE 0 END), 0) AS guestCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'DELIVERY' THEN 1 ELSE 0 END), 0) AS deliveryCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'SERVICE' THEN 1 ELSE 0 END), 0) AS serviceCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'FAMILY' THEN 1 ELSE 0 END), 0) AS familyCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'OTHER' THEN 1 ELSE 0 END), 0) AS otherCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'GUEST' AND ac.created_at >= date_trunc('day', NOW()) THEN 1 ELSE 0 END), 0) AS todayGuestCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'DELIVERY' AND ac.created_at >= date_trunc('day', NOW()) THEN 1 ELSE 0 END), 0) AS todayDeliveryCount,
                COALESCE(SUM(CASE WHEN ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW() THEN 1 ELSE 0 END), 0) AS activeQrCodes
            FROM access_codes ac
            JOIN users u ON u.id = ac.resident_id
            LEFT JOIN resident_profiles rp ON rp.user_id = u.id
            LEFT JOIN units un ON un.id = rp.unit_id
            WHERE u.role = 'RESIDENT'
              AND (
                    :searchTerm IS NULL
                    OR LOWER(ac.visitor_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(ac.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(COALESCE(un.unit_number, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                  )
              AND (
                    :status IS NULL
                    OR (:status = 'ACTIVE' AND ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW())
                    OR (:status = 'USED' AND ac.status = 'USED')
                    OR (:status = 'REVOKED' AND ac.status = 'REVOKED')
                    OR (:status = 'EXPIRED' AND (ac.status = 'EXPIRED' OR (ac.status = 'ACTIVE' AND ac.valid_until < NOW())))
                  )
            """,
            nativeQuery = true)
    AdminQrAccessSummaryRow getSummary(
            @Param("searchTerm") String searchTerm,
            @Param("status") String status
    );

    @Query(value = """
            SELECT
                ac.id AS accessCodeId,
                ac.code AS accessCode,
                ac.resident_id AS residentId,
                u.first_name AS residentFirstName,
                u.last_name AS residentLastName,
                ac.visitor_name AS visitorName,
                ac.visitor_phone AS visitorPhone,
                ac.purpose AS purpose,
                un.unit_number AS unitNumber,
                ac.created_at AS createdAt,
                ac.valid_until AS validUntil,
                CASE
                    WHEN ac.status = 'ACTIVE' AND ac.valid_until < NOW() THEN 'EXPIRED'
                    ELSE ac.status
                END AS effectiveStatus
            FROM access_codes ac
            JOIN users u ON u.id = ac.resident_id
            LEFT JOIN resident_profiles rp ON rp.user_id = u.id
            LEFT JOIN units un ON un.id = rp.unit_id
            WHERE ac.resident_id = :residentId
              AND u.role = 'RESIDENT'
              AND (
                    :status IS NULL
                    OR (:status = 'ACTIVE' AND ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW())
                    OR (:status = 'USED' AND ac.status = 'USED')
                    OR (:status = 'REVOKED' AND ac.status = 'REVOKED')
                    OR (:status = 'EXPIRED' AND (ac.status = 'EXPIRED' OR (ac.status = 'ACTIVE' AND ac.valid_until < NOW())))
                  )
              AND (:purpose IS NULL OR ac.purpose = :purpose)
            ORDER BY ac.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM access_codes ac
            JOIN users u ON u.id = ac.resident_id
            WHERE ac.resident_id = :residentId
              AND u.role = 'RESIDENT'
              AND (
                    :status IS NULL
                    OR (:status = 'ACTIVE' AND ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW())
                    OR (:status = 'USED' AND ac.status = 'USED')
                    OR (:status = 'REVOKED' AND ac.status = 'REVOKED')
                    OR (:status = 'EXPIRED' AND (ac.status = 'EXPIRED' OR (ac.status = 'ACTIVE' AND ac.valid_until < NOW())))
                  )
              AND (:purpose IS NULL OR ac.purpose = :purpose)
            """,
            nativeQuery = true)
    Page<AdminQrAccessRow> findForResidentAdmin(
            @Param("residentId") UUID residentId,
            @Param("status") String status,
            @Param("purpose") String purpose,
            Pageable pageable
    );

    @Query(value = """
            SELECT
                COUNT(*) AS totalCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'GUEST' THEN 1 ELSE 0 END), 0) AS guestCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'DELIVERY' THEN 1 ELSE 0 END), 0) AS deliveryCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'SERVICE' THEN 1 ELSE 0 END), 0) AS serviceCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'FAMILY' THEN 1 ELSE 0 END), 0) AS familyCount,
                COALESCE(SUM(CASE WHEN ac.purpose = 'OTHER' THEN 1 ELSE 0 END), 0) AS otherCount,
                COALESCE(SUM(CASE WHEN ac.status = 'ACTIVE' AND ac.valid_from <= NOW() AND ac.valid_until >= NOW() THEN 1 ELSE 0 END), 0) AS activeCount,
                COALESCE(SUM(CASE WHEN ac.status = 'USED' THEN 1 ELSE 0 END), 0) AS usedCount,
                COALESCE(SUM(CASE WHEN ac.status = 'EXPIRED' OR (ac.status = 'ACTIVE' AND ac.valid_until < NOW()) THEN 1 ELSE 0 END), 0) AS expiredCount,
                COALESCE(SUM(CASE WHEN ac.status = 'REVOKED' THEN 1 ELSE 0 END), 0) AS revokedCount
            FROM access_codes ac
            JOIN users u ON u.id = ac.resident_id
            WHERE ac.resident_id = :residentId
              AND u.role = 'RESIDENT'
            """,
            nativeQuery = true)
    AdminQrResidentSummaryRow getResidentSummaryForAdmin(@Param("residentId") UUID residentId);
}
