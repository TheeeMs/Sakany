package com.theMs.sakany.access.internal.application.queries;

import com.theMs.sakany.access.internal.domain.AccessCodeStatus;
import com.theMs.sakany.access.internal.infrastructure.persistence.AccessCodeEntity;
import com.theMs.sakany.access.internal.infrastructure.persistence.AccessCodeJpaRepository;
import com.theMs.sakany.access.internal.infrastructure.persistence.AdminQrAccessDirectoryJpaRepository;
import com.theMs.sakany.access.internal.infrastructure.persistence.AdminQrResidentHeaderRow;
import com.theMs.sakany.access.internal.infrastructure.persistence.AdminQrResidentSummaryRow;
import com.theMs.sakany.access.internal.infrastructure.persistence.AdminQrAccessRow;
import com.theMs.sakany.access.internal.infrastructure.persistence.AdminQrAccessSummaryRow;
import com.theMs.sakany.access.internal.infrastructure.persistence.VisitLogEntity;
import com.theMs.sakany.access.internal.infrastructure.persistence.VisitLogJpaRepository;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminQrAccessDirectoryService {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminQrAccessDirectoryJpaRepository adminQrAccessDirectoryJpaRepository;
        private final AccessCodeJpaRepository accessCodeJpaRepository;
        private final VisitLogJpaRepository visitLogJpaRepository;

        public AdminQrAccessDirectoryService(
                        AdminQrAccessDirectoryJpaRepository adminQrAccessDirectoryJpaRepository,
                        AccessCodeJpaRepository accessCodeJpaRepository,
                        VisitLogJpaRepository visitLogJpaRepository
        ) {
        this.adminQrAccessDirectoryJpaRepository = adminQrAccessDirectoryJpaRepository;
                this.accessCodeJpaRepository = accessCodeJpaRepository;
                this.visitLogJpaRepository = visitLogJpaRepository;
    }

    public AdminQrAccessPage listCodes(
            AdminQrAccessTab tab,
            String search,
            AdminQrAccessStatus status,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String purposeFilter = tab == null || tab == AdminQrAccessTab.ALL ? null : tab.name();
        String statusFilter = status == null ? null : status.name();
        String normalizedSearch = normalize(search);

        Page<AdminQrAccessRow> rows = adminQrAccessDirectoryJpaRepository.findForAdmin(
                normalizedSearch,
                statusFilter,
                purposeFilter,
                pageable
        );

        AdminQrAccessSummaryRow summary = adminQrAccessDirectoryJpaRepository.getSummary(
                normalizedSearch,
                statusFilter
        );

        List<AdminQrAccessItem> items = rows.getContent().stream()
                .map(this::mapRow)
                .toList();

        long totalCount = summary != null ? safeLong(summary.getTotalCount()) : rows.getTotalElements();
        long guestCount = summary != null ? safeLong(summary.getGuestCount()) : 0L;
        long deliveryCount = summary != null ? safeLong(summary.getDeliveryCount()) : 0L;
        long serviceCount = summary != null ? safeLong(summary.getServiceCount()) : 0L;
        long familyCount = summary != null ? safeLong(summary.getFamilyCount()) : 0L;
        long otherCount = summary != null ? safeLong(summary.getOtherCount()) : 0L;

        return new AdminQrAccessPage(
                items,
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious(),
                new AdminQrAccessSummary(
                        totalCount,
                        guestCount,
                        deliveryCount,
                        serviceCount,
                        familyCount,
                        otherCount,
                        summary != null ? safeLong(summary.getTodayGuestCount()) : 0L,
                        summary != null ? safeLong(summary.getTodayDeliveryCount()) : 0L,
                        summary != null ? safeLong(summary.getActiveQrCodes()) : 0L
                ),
                List.of(
                        new AdminQrAccessTabCount(AdminQrAccessTab.ALL.name(), totalCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.GUEST.name(), guestCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.DELIVERY.name(), deliveryCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.SERVICE.name(), serviceCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.FAMILY.name(), familyCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.OTHER.name(), otherCount)
                )
        );
    }

    public ResidentQrCodesResponse listResidentCodes(
            UUID residentId,
            AdminQrAccessTab tab,
            AdminQrAccessStatus status,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String purposeFilter = tab == null || tab == AdminQrAccessTab.ALL ? null : tab.name();
        String statusFilter = status == null ? null : status.name();

        AdminQrResidentHeaderRow header = adminQrAccessDirectoryJpaRepository.findResidentHeaderForAdmin(residentId)
                .orElseThrow(() -> new NotFoundException("Resident", residentId));

        Page<AdminQrAccessRow> rows = adminQrAccessDirectoryJpaRepository.findForResidentAdmin(
                residentId,
                statusFilter,
                purposeFilter,
                pageable
        );

        AdminQrResidentSummaryRow summary = adminQrAccessDirectoryJpaRepository.getResidentSummaryForAdmin(residentId);

        List<ResidentQrCodeItem> items = rows.getContent().stream()
                .map(this::mapResidentRow)
                .toList();

        long totalCount = summary != null ? safeLong(summary.getTotalCount()) : rows.getTotalElements();
        long guestCount = summary != null ? safeLong(summary.getGuestCount()) : 0L;
        long deliveryCount = summary != null ? safeLong(summary.getDeliveryCount()) : 0L;
        long serviceCount = summary != null ? safeLong(summary.getServiceCount()) : 0L;
        long familyCount = summary != null ? safeLong(summary.getFamilyCount()) : 0L;
        long otherCount = summary != null ? safeLong(summary.getOtherCount()) : 0L;

        return new ResidentQrCodesResponse(
                new ResidentQrHeader(
                        header.getResidentId(),
                        buildFullName(header.getFirstName(), header.getLastName()),
                        buildInitials(header.getFirstName(), header.getLastName()),
                        header.getUnitNumber(),
                        header.getBuildingName(),
                        header.getPhoneNumber()
                ),
                items,
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious(),
                new ResidentQrSummary(
                        totalCount,
                        guestCount,
                        deliveryCount,
                        serviceCount,
                        familyCount,
                        otherCount,
                        summary != null ? safeLong(summary.getActiveCount()) : 0L,
                        summary != null ? safeLong(summary.getUsedCount()) : 0L,
                        summary != null ? safeLong(summary.getExpiredCount()) : 0L,
                        summary != null ? safeLong(summary.getRevokedCount()) : 0L
                ),
                List.of(
                        new AdminQrAccessTabCount(AdminQrAccessTab.ALL.name(), totalCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.GUEST.name(), guestCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.DELIVERY.name(), deliveryCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.SERVICE.name(), serviceCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.FAMILY.name(), familyCount),
                        new AdminQrAccessTabCount(AdminQrAccessTab.OTHER.name(), otherCount)
                ),
                List.of(
                        new AdminQrStatusCount(AdminQrAccessStatus.ACTIVE.name(), summary != null ? safeLong(summary.getActiveCount()) : 0L),
                        new AdminQrStatusCount(AdminQrAccessStatus.USED.name(), summary != null ? safeLong(summary.getUsedCount()) : 0L),
                        new AdminQrStatusCount(AdminQrAccessStatus.EXPIRED.name(), summary != null ? safeLong(summary.getExpiredCount()) : 0L),
                        new AdminQrStatusCount(AdminQrAccessStatus.REVOKED.name(), summary != null ? safeLong(summary.getRevokedCount()) : 0L)
                )
        );
    }

    public AdminQrCodeDetailsResponse getCodeDetails(UUID accessCodeId) {
        AdminQrAccessRow row = adminQrAccessDirectoryJpaRepository.findCodeByIdForAdmin(accessCodeId)
                .orElseThrow(() -> new NotFoundException("AccessCode", accessCodeId));

        AccessCodeEntity accessCode = accessCodeJpaRepository.findById(accessCodeId)
                .orElseThrow(() -> new NotFoundException("AccessCode", accessCodeId));

        String residentName = buildFullName(row.getResidentFirstName(), row.getResidentLastName());
        String status = effectiveStatus(accessCode).name();

        return new AdminQrCodeDetailsResponse(
                row.getAccessCodeId(),
                row.getAccessCode(),
                accessCode.getQrData(),
                row.getVisitorName(),
                row.getVisitorPhone(),
                row.getPurpose(),
                mapPurposeSubtitle(row.getPurpose() == null ? "OTHER" : row.getPurpose()),
                residentName,
                row.getUnitNumber(),
                status,
                row.getCreatedAt(),
                row.getValidUntil(),
                accessCode.isSingleUse(),
                "/v1/admin/access/codes/" + row.getAccessCodeId() + "/download",
                "/v1/admin/access/codes/" + row.getAccessCodeId(),
                buildHistory(accessCode, residentName)
        );
    }

    public byte[] downloadCode(UUID accessCodeId) {
        AccessCodeEntity accessCode = accessCodeJpaRepository.findById(accessCodeId)
                .orElseThrow(() -> new NotFoundException("AccessCode", accessCodeId));

        String content = "QR Code: " + accessCode.getCode() + "\n"
                + "Visitor: " + accessCode.getVisitorName() + "\n"
                + "QR Data: " + accessCode.getQrData() + "\n"
                + "Valid Until: " + accessCode.getValidUntil() + "\n";

        return content.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void deleteCode(UUID accessCodeId) {
        AccessCodeEntity accessCode = accessCodeJpaRepository.findById(accessCodeId)
                .orElseThrow(() -> new NotFoundException("AccessCode", accessCodeId));

        if (accessCode.getStatus() != AccessCodeStatus.REVOKED) {
            accessCode.setStatus(AccessCodeStatus.REVOKED);
            accessCodeJpaRepository.save(accessCode);
        }
    }

    private AdminQrAccessItem mapRow(AdminQrAccessRow row) {
        String hostResidentName = ((row.getResidentFirstName() == null ? "" : row.getResidentFirstName())
                + " "
                + (row.getResidentLastName() == null ? "" : row.getResidentLastName())).trim();

        String purpose = row.getPurpose() == null ? "OTHER" : row.getPurpose();

        return new AdminQrAccessItem(
                row.getAccessCodeId(),
                row.getAccessCode(),
                row.getVisitorName(),
                row.getVisitorPhone(),
                mapPurposeSubtitle(purpose),
                purpose,
                row.getResidentId(),
                hostResidentName,
                row.getUnitNumber(),
                row.getCreatedAt(),
                row.getValidUntil(),
                row.getEffectiveStatus()
        );
    }

    private String mapPurposeSubtitle(String purpose) {
        return switch (purpose) {
            case "DELIVERY" -> "Package delivery";
            case "SERVICE" -> "Maintenance service";
            case "FAMILY" -> "Family visit";
            case "GUEST" -> "Social visit";
            default -> "Other visit";
        };
    }

        private List<AdminQrHistoryItem> buildHistory(AccessCodeEntity accessCode, String residentName) {
                List<AdminQrHistoryItem> history = new ArrayList<>();

                Instant createdAt = accessCode.getCreatedAt();
                if (createdAt != null) {
                        history.add(new AdminQrHistoryItem(
                                        "QR Code Generated",
                                        createdAt,
                                        residentName,
                                        null
                        ));

                        if (accessCode.getVisitorPhone() != null && !accessCode.getVisitorPhone().isBlank()) {
                                history.add(new AdminQrHistoryItem(
                                                "QR Code Sent via SMS",
                                                createdAt.plus(Duration.ofMinutes(1)),
                                                "System",
                                                "Sent to " + accessCode.getVisitorPhone()
                                ));
                        }
                }

                List<VisitLogEntity> visitLogs = visitLogJpaRepository.findByAccessCodeId(accessCode.getId()).stream()
                                .sorted(Comparator.comparing(VisitLogEntity::getEntryTime, Comparator.nullsLast(Comparator.naturalOrder())))
                                .toList();

                for (VisitLogEntity log : visitLogs) {
                        if (log.getEntryTime() != null) {
                                String gateDetails = log.getGateNumber() == null || log.getGateNumber().isBlank()
                                                ? null
                                                : "Gate " + log.getGateNumber();
                                history.add(new AdminQrHistoryItem(
                                                "QR Code Scanned",
                                                log.getEntryTime(),
                                                "Security",
                                                gateDetails
                                ));
                        }
                        if (log.getExitTime() != null) {
                                history.add(new AdminQrHistoryItem(
                                                "Visitor Exit Logged",
                                                log.getExitTime(),
                                                "Security",
                                                null
                                ));
                        }
                }

                if (accessCode.getStatus() == AccessCodeStatus.REVOKED) {
                        history.add(new AdminQrHistoryItem(
                                        "QR Code Deleted",
                                        accessCode.getUpdatedAt() != null ? accessCode.getUpdatedAt() : Instant.now(),
                                        "Admin",
                                        null
                        ));
                }

                if (effectiveStatus(accessCode) == AccessCodeStatus.EXPIRED) {
                        history.add(new AdminQrHistoryItem(
                                        "QR Code Expired",
                                        accessCode.getValidUntil(),
                                        "System",
                                        null
                        ));
                }

                return history.stream()
                                .sorted(Comparator.comparing(AdminQrHistoryItem::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())))
                                .toList();
        }

        private AccessCodeStatus effectiveStatus(AccessCodeEntity entity) {
                if (entity.getStatus() == AccessCodeStatus.ACTIVE
                                && entity.getValidUntil() != null
                                && entity.getValidUntil().isBefore(Instant.now())) {
                        return AccessCodeStatus.EXPIRED;
                }
                return entity.getStatus();
        }

        private ResidentQrCodeItem mapResidentRow(AdminQrAccessRow row) {
                String purpose = row.getPurpose() == null ? "OTHER" : row.getPurpose();
                return new ResidentQrCodeItem(
                                row.getAccessCodeId(),
                                row.getAccessCode(),
                                row.getVisitorName(),
                                row.getVisitorPhone(),
                                mapPurposeSubtitle(purpose),
                                purpose,
                                row.getCreatedAt(),
                                row.getValidUntil(),
                                row.getEffectiveStatus()
                );
        }

        private String buildFullName(String firstName, String lastName) {
                String f = firstName == null ? "" : firstName.trim();
                String l = lastName == null ? "" : lastName.trim();
                String full = (f + " " + l).trim();
                return full.isEmpty() ? "Unknown Resident" : full;
        }

        private String buildInitials(String firstName, String lastName) {
                StringBuilder initials = new StringBuilder();
                if (firstName != null && !firstName.isBlank()) {
                        initials.append(Character.toUpperCase(firstName.trim().charAt(0)));
                }
                if (lastName != null && !lastName.isBlank()) {
                        initials.append(Character.toUpperCase(lastName.trim().charAt(0)));
                }
                return initials.isEmpty() ? "NA" : initials.toString();
        }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record AdminQrAccessPage(
            List<AdminQrAccessItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            AdminQrAccessSummary summary,
            List<AdminQrAccessTabCount> tabs
    ) {
    }

    public record AdminQrAccessItem(
            UUID accessCodeId,
            String qrCode,
            String visitorName,
            String visitorPhone,
            String visitorSubtitle,
            String visitorType,
            UUID hostResidentId,
            String hostResidentName,
            String unitNumber,
            Instant createdAt,
            Instant validUntil,
            String status
    ) {
    }

    public record AdminQrAccessSummary(
            long totalCount,
            long guestCount,
            long deliveryCount,
            long serviceCount,
            long familyCount,
            long otherCount,
            long todayGuestCount,
            long todayDeliveryCount,
            long activeQrCodes
    ) {
    }

    public record AdminQrAccessTabCount(
            String tab,
            long count
    ) {
    }

    public record ResidentQrCodesResponse(
            ResidentQrHeader resident,
            List<ResidentQrCodeItem> codes,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            ResidentQrSummary summary,
            List<AdminQrAccessTabCount> typeTabs,
            List<AdminQrStatusCount> statusTabs
    ) {
    }

    public record ResidentQrHeader(
            UUID residentId,
            String fullName,
            String initials,
            String unitNumber,
            String buildingName,
            String phoneNumber
    ) {
    }

    public record ResidentQrCodeItem(
            UUID accessCodeId,
            String qrCode,
            String visitorName,
            String visitorPhone,
            String visitorSubtitle,
            String visitorType,
            Instant createdAt,
            Instant validUntil,
            String status
    ) {
    }

    public record ResidentQrSummary(
            long totalCount,
            long guestCount,
            long deliveryCount,
            long serviceCount,
            long familyCount,
            long otherCount,
            long activeCount,
            long usedCount,
            long expiredCount,
            long revokedCount
    ) {
    }

    public record AdminQrStatusCount(
            String status,
            long count
    ) {
    }

    public record AdminQrCodeDetailsResponse(
            UUID accessCodeId,
            String qrCode,
            String qrData,
            String visitorName,
            String visitorPhone,
            String visitorType,
            String visitorSubtitle,
            String hostResidentName,
            String unitNumber,
            String status,
            Instant createdAt,
            Instant validUntil,
            boolean singleUse,
            String downloadUrl,
            String deleteUrl,
            List<AdminQrHistoryItem> history
    ) {
    }

    public record AdminQrHistoryItem(
            String title,
            Instant occurredAt,
            String actor,
            String details
    ) {
    }
}
