package com.theMs.sakany.accounts.internal.application.queries;

import com.theMs.sakany.access.internal.infrastructure.persistence.AccessCodeEntity;
import com.theMs.sakany.access.internal.infrastructure.persistence.AccessCodeJpaRepository;
import com.theMs.sakany.accounts.internal.domain.ResidentApprovalStatus;
import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.billing.internal.domain.InvoiceType;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import com.theMs.sakany.billing.internal.infrastructure.persistence.InvoiceEntity;
import com.theMs.sakany.billing.internal.infrastructure.persistence.InvoiceJpaRepository;
import com.theMs.sakany.billing.internal.infrastructure.persistence.PaymentEntity;
import com.theMs.sakany.billing.internal.infrastructure.persistence.PaymentJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitJpaRepository;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminResidentCardService {

    private final UserJpaRepository userJpaRepository;
    private final ResidentProfileJpaRepository residentProfileJpaRepository;
    private final UnitJpaRepository unitJpaRepository;
    private final BuildingJpaRepository buildingJpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;
    private final InvoiceJpaRepository invoiceJpaRepository;
    private final AccessCodeJpaRepository accessCodeJpaRepository;

    public AdminResidentCardService(
            UserJpaRepository userJpaRepository,
            ResidentProfileJpaRepository residentProfileJpaRepository,
            UnitJpaRepository unitJpaRepository,
            BuildingJpaRepository buildingJpaRepository,
            PaymentJpaRepository paymentJpaRepository,
            InvoiceJpaRepository invoiceJpaRepository,
            AccessCodeJpaRepository accessCodeJpaRepository
    ) {
        this.userJpaRepository = userJpaRepository;
        this.residentProfileJpaRepository = residentProfileJpaRepository;
        this.unitJpaRepository = unitJpaRepository;
        this.buildingJpaRepository = buildingJpaRepository;
        this.paymentJpaRepository = paymentJpaRepository;
        this.invoiceJpaRepository = invoiceJpaRepository;
        this.accessCodeJpaRepository = accessCodeJpaRepository;
    }

    public ResidentCardResponse getResidentCard(UUID residentId) {
        UserEntity resident = userJpaRepository.findById(residentId)
                .orElseThrow(() -> new NotFoundException("Resident", residentId));
        if (resident.getRole() != Role.RESIDENT) {
            throw new NotFoundException("Resident", residentId);
        }

        ResidentProfileEntity profile = residentProfileJpaRepository.findByUserId(residentId)
                .orElseThrow(() -> new NotFoundException("ResidentProfile", residentId));

        UnitEntity unit = profile.getUnitId() == null
                ? null
                : unitJpaRepository.findById(profile.getUnitId()).orElse(null);

        BuildingEntity building = unit == null
                ? null
                : buildingJpaRepository.findById(unit.getBuildingId()).orElse(null);

        BigDecimal monthlyFee = resolveMonthlyFee(profile, residentId);
        String contractStatus = deriveContractStatus(resident.isActive(), profile.getApprovalStatus());

        return new ResidentCardResponse(
                residentId,
                buildFullName(resident.getFirstName(), resident.getLastName()),
                buildInitials(resident.getFirstName(), resident.getLastName()),
                building != null ? building.getName() : null,
                unit != null ? unit.getUnitNumber() : null,
                new PersonalInformation(
                        resident.getEmail(),
                        resident.getPhone(),
                        profile.getNationalId()
                ),
                new LeaseUnitInformation(
                        profile.getMoveInDate(),
                        contractStatus,
                        monthlyFee,
                        "EGP"
                ),
                getRecentActivities(residentId)
        );
    }

    private BigDecimal resolveMonthlyFee(ResidentProfileEntity profile, UUID residentId) {
        if (profile.getMonthlyFee() != null) {
            return profile.getMonthlyFee();
        }

        return invoiceJpaRepository.findByResidentId(residentId).stream()
                .filter(i -> i.getType() == InvoiceType.MONTHLY_FEE)
                .max(Comparator.comparing(InvoiceEntity::getIssuedAt))
                .map(InvoiceEntity::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    private List<RecentActivityItem> getRecentActivities(UUID residentId) {
        List<RecentActivityItem> activities = new ArrayList<>();

        for (PaymentEntity payment : paymentJpaRepository.findByResidentId(residentId)) {
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                continue;
            }

            Optional<InvoiceEntity> invoiceOpt = invoiceJpaRepository.findById(payment.getInvoiceId());
            String title = invoiceOpt
                    .map(invoice -> switch (invoice.getType()) {
                        case MONTHLY_FEE -> "Paid Rent";
                        case MAINTENANCE_CHARGE -> "Paid Maintenance";
                        default -> "Payment Completed";
                    })
                    .orElse("Payment Completed");

            String currency = invoiceOpt.map(InvoiceEntity::getCurrency).orElse("EGP");

            activities.add(new RecentActivityItem(
                    title,
                    payment.getCreatedAt(),
                    payment.getAmount(),
                    currency
            ));
        }

        for (AccessCodeEntity code : accessCodeJpaRepository.findByResidentId(residentId)) {
            activities.add(new RecentActivityItem(
                    "Gate Code Generated",
                    code.getCreatedAt(),
                    null,
                    null
            ));
        }

        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityItem::occurredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();
    }

    private String deriveContractStatus(boolean isActive, ResidentApprovalStatus approvalStatus) {
        if (!isActive) {
            return "Inactive";
        }
        if (approvalStatus == ResidentApprovalStatus.REJECTED) {
            return "Rejected";
        }
        if (approvalStatus == ResidentApprovalStatus.PENDING) {
            return "Pending";
        }
        return "Active";
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

    public record ResidentCardResponse(
            UUID residentId,
            String fullName,
            String initials,
            String buildingName,
            String unitNumber,
            PersonalInformation personalInformation,
            LeaseUnitInformation leaseUnitInformation,
            List<RecentActivityItem> recentActivity
    ) {
    }

    public record PersonalInformation(
            String email,
            String phone,
            String nationalId
    ) {
    }

    public record LeaseUnitInformation(
            LocalDate moveInDate,
            String contractStatus,
            BigDecimal monthlyFee,
            String currency
    ) {
    }

    public record RecentActivityItem(
            String title,
            Instant occurredAt,
            BigDecimal amount,
            String currency
    ) {
    }
}
