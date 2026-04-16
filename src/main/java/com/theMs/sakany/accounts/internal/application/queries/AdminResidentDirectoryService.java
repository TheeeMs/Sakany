package com.theMs.sakany.accounts.internal.application.queries;

import com.theMs.sakany.accounts.internal.domain.ResidentApprovalStatus;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.AdminResidentBuildingOptionRow;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.AdminResidentDirectoryJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.AdminResidentDirectoryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminResidentDirectoryService {

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminResidentDirectoryJpaRepository directoryRepository;

    public AdminResidentDirectoryService(AdminResidentDirectoryJpaRepository directoryRepository) {
        this.directoryRepository = directoryRepository;
    }

    public AdminResidentDirectoryPage listResidents(
            String search,
            UUID buildingId,
            ResidentDirectoryStatus status,
            ResidentApprovalStatus approvalStatus,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String normalizedSearch = normalizeSearch(search);
        Page<AdminResidentDirectoryRow> rows = directoryRepository.findResidentsForAdmin(
                normalizedSearch,
                buildingId,
                status != null ? status.name() : null,
                approvalStatus != null ? approvalStatus.name() : null,
                pageable
        );

        List<AdminResidentDirectoryItem> residents = rows.getContent().stream()
                .map(this::mapRow)
                .toList();

        return new AdminResidentDirectoryPage(
                residents,
                rows.getNumber(),
                rows.getSize(),
                rows.getTotalElements(),
                rows.getTotalPages(),
                rows.hasNext(),
                rows.hasPrevious()
        );
    }

    public Optional<AdminResidentDirectoryItem> getResident(UUID residentId) {
        return directoryRepository.findResidentForAdmin(residentId).map(this::mapRow);
    }

    public List<AdminResidentBuildingOption> getResidentBuildingOptions() {
        return directoryRepository.findResidentBuildingOptions().stream()
                .map(this::mapBuilding)
                .toList();
    }

    private AdminResidentBuildingOption mapBuilding(AdminResidentBuildingOptionRow row) {
        return new AdminResidentBuildingOption(row.getBuildingId(), row.getBuildingName());
    }

    private AdminResidentDirectoryItem mapRow(AdminResidentDirectoryRow row) {
        BigDecimal dueAmount = row.getDueAmount() == null ? BigDecimal.ZERO : row.getDueAmount();
        ResidentApprovalStatus approvalStatus = parseApprovalStatus(row.getApprovalStatus());
        ResidentDirectoryStatus residentStatus = deriveResidentStatus(Boolean.TRUE.equals(row.getActive()), approvalStatus);
        String financialStatus = dueAmount.compareTo(BigDecimal.ZERO) > 0 ? "DUE" : "CLEAR";

        return new AdminResidentDirectoryItem(
                row.getResidentId(),
                row.getProfileId(),
                buildFullName(row.getFirstName(), row.getLastName()),
                row.getFirstName(),
                row.getLastName(),
                residentStatus.name(),
                row.getPhoneNumber(),
                row.getEmail(),
                Boolean.TRUE.equals(row.getActive()),
                Boolean.TRUE.equals(row.getPhoneVerified()),
                approvalStatus,
                row.getResidentType(),
                row.getMoveInDate(),
                row.getUnitId(),
                row.getUnitNumber(),
                row.getBuildingId(),
                row.getBuildingName(),
                financialStatus,
                dueAmount,
                row.getCurrency(),
                row.getCreatedAt()
        );
    }

    private ResidentDirectoryStatus deriveResidentStatus(boolean isActive, ResidentApprovalStatus approvalStatus) {
        if (!isActive) {
            return ResidentDirectoryStatus.INACTIVE;
        }
        if (approvalStatus == ResidentApprovalStatus.PENDING) {
            return ResidentDirectoryStatus.PENDING;
        }
        return ResidentDirectoryStatus.ACTIVE;
    }

    private ResidentApprovalStatus parseApprovalStatus(String value) {
        if (value == null || value.isBlank()) {
            return ResidentApprovalStatus.PENDING;
        }
        return ResidentApprovalStatus.valueOf(value.trim().toUpperCase());
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildFullName(String firstName, String lastName) {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Unknown Resident" : full;
    }

    public record AdminResidentDirectoryItem(
            UUID residentId,
            UUID profileId,
            String fullName,
            String firstName,
            String lastName,
            String residentStatus,
            String phoneNumber,
            String email,
            boolean isActive,
            boolean isPhoneVerified,
            ResidentApprovalStatus approvalStatus,
            String residentType,
            LocalDate moveInDate,
            UUID unitId,
            String unitNumber,
            UUID buildingId,
            String buildingName,
            String financialStatus,
            BigDecimal dueAmount,
            String currency,
            Instant createdAt
    ) {
    }

    public record AdminResidentDirectoryPage(
            List<AdminResidentDirectoryItem> residents,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {
    }

    public record AdminResidentBuildingOption(
            UUID buildingId,
            String buildingName
    ) {
    }
}
