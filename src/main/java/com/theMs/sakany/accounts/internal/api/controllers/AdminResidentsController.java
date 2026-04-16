package com.theMs.sakany.accounts.internal.api.controllers;

import com.theMs.sakany.accounts.internal.application.commands.CreateUserCommand;
import com.theMs.sakany.accounts.internal.application.commands.CreateUserCommandHandler;
import com.theMs.sakany.accounts.internal.application.queries.AdminResidentCardService;
import com.theMs.sakany.accounts.internal.application.queries.AdminResidentDirectoryService;
import com.theMs.sakany.accounts.internal.application.queries.ResidentDirectoryStatus;
import com.theMs.sakany.accounts.internal.domain.LoginMethod;
import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.accounts.internal.domain.ResidentApprovalStatus;
import com.theMs.sakany.accounts.internal.domain.ResidentType;
import com.theMs.sakany.accounts.internal.domain.User;
import com.theMs.sakany.accounts.internal.domain.UserRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.ResidentProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.billing.internal.application.commands.IssueInvoiceCommand;
import com.theMs.sakany.billing.internal.application.commands.IssueInvoiceCommandHandler;
import com.theMs.sakany.billing.internal.domain.InvoiceType;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.BuildingJpaRepository;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitEntity;
import com.theMs.sakany.property.internal.infrastructure.persistence.UnitJpaRepository;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.security.SecureRandom;

@RestController
@RequestMapping("/v1/admin/residents")
public class AdminResidentsController {

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final AdminResidentCardService adminResidentCardService;
    private final AdminResidentDirectoryService residentDirectoryService;
    private final CreateUserCommandHandler createUserCommandHandler;
    private final IssueInvoiceCommandHandler issueInvoiceCommandHandler;
    private final UserRepository userRepository;
    private final UserJpaRepository userJpaRepository;
    private final ResidentProfileJpaRepository residentProfileJpaRepository;
    private final UnitJpaRepository unitJpaRepository;
    private final BuildingJpaRepository buildingJpaRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminResidentsController(
            AdminResidentCardService adminResidentCardService,
            AdminResidentDirectoryService residentDirectoryService,
            CreateUserCommandHandler createUserCommandHandler,
            IssueInvoiceCommandHandler issueInvoiceCommandHandler,
            UserRepository userRepository,
            UserJpaRepository userJpaRepository,
            ResidentProfileJpaRepository residentProfileJpaRepository,
            UnitJpaRepository unitJpaRepository,
            BuildingJpaRepository buildingJpaRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.adminResidentCardService = adminResidentCardService;
        this.residentDirectoryService = residentDirectoryService;
        this.createUserCommandHandler = createUserCommandHandler;
        this.issueInvoiceCommandHandler = issueInvoiceCommandHandler;
        this.userRepository = userRepository;
        this.userJpaRepository = userJpaRepository;
        this.residentProfileJpaRepository = residentProfileJpaRepository;
        this.unitJpaRepository = unitJpaRepository;
        this.buildingJpaRepository = buildingJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<AdminResidentDirectoryService.AdminResidentDirectoryPage> listResidents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) ResidentDirectoryStatus status,
            @RequestParam(required = false) ResidentApprovalStatus approvalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        AdminResidentDirectoryService.AdminResidentDirectoryPage response = residentDirectoryService.listResidents(
                search,
                buildingId,
                status,
                approvalStatus,
                page,
                size
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{residentId}")
    public ResponseEntity<AdminResidentDirectoryService.AdminResidentDirectoryItem> getResident(@PathVariable UUID residentId) {
        return residentDirectoryService.getResident(residentId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException("Resident", residentId));
    }

    @GetMapping("/{residentId}/card")
    public ResponseEntity<AdminResidentCardService.ResidentCardResponse> getResidentCard(@PathVariable UUID residentId) {
        return ResponseEntity.ok(adminResidentCardService.getResidentCard(residentId));
    }

    @GetMapping("/buildings")
    public ResponseEntity<List<AdminResidentDirectoryService.AdminResidentBuildingOption>> listResidentBuildings() {
        return ResponseEntity.ok(residentDirectoryService.getResidentBuildingOptions());
    }

    @GetMapping("/units")
    public ResponseEntity<List<ResidentUnitOptionResponse>> listResidentUnits(
            @RequestParam(required = false) UUID buildingId
    ) {
        List<UnitEntity> units = buildingId == null
                ? unitJpaRepository.findAll()
                : unitJpaRepository.findByBuildingId(buildingId);

        Map<UUID, BuildingEntity> buildingsById = buildingJpaRepository.findAllById(
                units.stream().map(UnitEntity::getBuildingId).distinct().toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(BuildingEntity::getId, b -> b));

        List<ResidentUnitOptionResponse> response = units.stream()
                .map(unit -> {
                    BuildingEntity building = buildingsById.get(unit.getBuildingId());
                    String buildingName = building != null ? building.getName() : null;
                    return new ResidentUnitOptionResponse(
                            unit.getId(),
                            unit.getBuildingId(),
                            buildingName,
                            unit.getUnitNumber(),
                            unit.getFloor(),
                            unit.getType().name()
                    );
                })
                .sorted((a, b) -> {
                    String ab = a.buildingName() == null ? "" : a.buildingName();
                    String bb = b.buildingName() == null ? "" : b.buildingName();
                    int buildingCmp = ab.compareToIgnoreCase(bb);
                    if (buildingCmp != 0) {
                        return buildingCmp;
                    }
                    return a.unitNumber().compareToIgnoreCase(b.unitNumber());
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UUID> createResident(@RequestBody CreateResidentRequest request) {
        validateResidentCreateRequest(request);

        LoginMethod loginMethod = request.loginMethod() == null ? LoginMethod.PHONE_OTP : request.loginMethod();

        UUID residentId = createUserCommandHandler.handle(new CreateUserCommand(
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.email(),
                request.password(),
                request.type(),
                request.unitId(),
            loginMethod,
            request.moveInDate()
        ));

        ResidentApprovalStatus effectiveApprovalStatus =
                request.approvalStatus() == null ? ResidentApprovalStatus.PENDING : request.approvalStatus();
        ResidentProfileEntity profile = residentProfileJpaRepository.findByUserId(residentId)
            .orElseThrow(() -> new NotFoundException("ResidentProfile", residentId));
        if (effectiveApprovalStatus != ResidentApprovalStatus.PENDING) {
            profile.setApprovalStatus(effectiveApprovalStatus);
        }
        if (request.nationalId() != null) {
            profile.setNationalId(request.nationalId());
        }
        if (request.monthlyFee() != null) {
            profile.setMonthlyFee(request.monthlyFee());
        }
        residentProfileJpaRepository.save(profile);

        if (request.monthlyFee() != null && request.monthlyFee().compareTo(BigDecimal.ZERO) > 0) {
            LocalDate dueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
            issueInvoiceCommandHandler.handle(new IssueInvoiceCommand(
                    residentId,
                    request.unitId(),
                    InvoiceType.MONTHLY_FEE,
                    request.monthlyFee(),
                    "EGP",
                    "Monthly fee",
                    dueDate
            ));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(residentId);
    }

    @PatchMapping("/{residentId}/approval")
    public ResponseEntity<Void> updateApprovalStatus(
            @PathVariable UUID residentId,
            @RequestBody UpdateResidentApprovalRequest request
    ) {
        if (request.approvalStatus() == null) {
            throw new BusinessRuleException("approvalStatus is required");
        }

        ResidentProfileEntity profile = residentProfileJpaRepository.findByUserId(residentId)
                .orElseThrow(() -> new NotFoundException("ResidentProfile", residentId));
        profile.setApprovalStatus(request.approvalStatus());
        residentProfileJpaRepository.save(profile);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{residentId}")
    public ResponseEntity<Void> updateResident(
            @PathVariable UUID residentId,
            @RequestBody UpdateResidentRequest request
    ) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        UserEntity resident = userJpaRepository.findById(residentId)
                .orElseThrow(() -> new NotFoundException("Resident", residentId));
        if (resident.getRole() != Role.RESIDENT) {
            throw new BusinessRuleException("Provided user is not a resident");
        }

        if (request.firstName() != null) {
            resident.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            resident.setLastName(request.lastName());
        }
        if (request.phoneNumber() != null) {
            resident.setPhone(request.phoneNumber());
        }
        if (request.email() != null) {
            resident.setEmail(request.email());
        }
        if (request.isActive() != null) {
            resident.setActive(request.isActive());
        }
        userJpaRepository.save(resident);

        ResidentProfileEntity profile = residentProfileJpaRepository.findByUserId(residentId)
                .orElseThrow(() -> new NotFoundException("ResidentProfile", residentId));
        if (request.unitId() != null) {
            profile.setUnitId(request.unitId());
        }
        if (request.type() != null) {
            profile.setType(request.type());
        }
        if (request.moveInDate() != null) {
            profile.setMoveInDate(request.moveInDate());
        }
        if (request.nationalId() != null) {
            profile.setNationalId(request.nationalId());
        }
        if (request.monthlyFee() != null) {
            profile.setMonthlyFee(request.monthlyFee());
        }
        residentProfileJpaRepository.save(profile);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{residentId}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetResidentPassword(
            @PathVariable UUID residentId,
            @RequestBody(required = false) ResetPasswordRequest request
    ) {
        User resident = userRepository.findById(residentId)
                .orElseThrow(() -> new NotFoundException("Resident", residentId));

        if (resident.getRole() != Role.RESIDENT) {
            throw new BusinessRuleException("Provided user is not a resident");
        }

        String rawPassword = request != null && request.newPassword() != null && !request.newPassword().isBlank()
                ? request.newPassword()
                : generateTempPassword(10);

        if (rawPassword.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters");
        }

        resident.setHashedPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(resident);

        return ResponseEntity.ok(new ResetPasswordResponse(
                residentId,
                "Password reset successfully",
                rawPassword
        ));
    }

    @DeleteMapping("/{residentId}")
    public ResponseEntity<Void> deactivateResident(@PathVariable UUID residentId) {
        User user = userRepository.findById(residentId)
                .orElseThrow(() -> new NotFoundException("Resident", residentId));

        if (user.isActive()) {
            user.deactivate();
            userRepository.save(user);
        }

        return ResponseEntity.noContent().build();
    }

    private void validateResidentCreateRequest(CreateResidentRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }
        if (request.unitId() == null) {
            throw new BusinessRuleException("unitId is required");
        }
        if (request.type() == null) {
            throw new BusinessRuleException("type is required");
        }
        if (request.loginMethod() == LoginMethod.EMAIL_PASSWORD
                && (request.password() == null || request.password().isBlank())) {
            throw new BusinessRuleException("password is required when loginMethod is EMAIL_PASSWORD");
        }
        if (request.monthlyFee() != null && request.monthlyFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("monthlyFee must be positive");
        }
    }

    private String generateTempPassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(TEMP_PASSWORD_CHARS.length());
            builder.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return builder.toString();
    }

    public record CreateResidentRequest(
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String password,
            ResidentType type,
            UUID unitId,
            LoginMethod loginMethod,
                ResidentApprovalStatus approvalStatus,
                LocalDate moveInDate,
                BigDecimal monthlyFee,
                String nationalId
    ) {
    }

    public record UpdateResidentApprovalRequest(ResidentApprovalStatus approvalStatus) {
    }

    public record UpdateResidentRequest(
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            Boolean isActive,
            UUID unitId,
            ResidentType type,
                LocalDate moveInDate,
                BigDecimal monthlyFee,
                String nationalId
    ) {
    }

            public record ResidentUnitOptionResponse(
                UUID unitId,
                UUID buildingId,
                String buildingName,
                String unitNumber,
                int floor,
                String unitType
            ) {
            }

                public record ResetPasswordRequest(String newPassword) {
                }

                public record ResetPasswordResponse(
                    UUID residentId,
                    String message,
                    String temporaryPassword
                ) {
                }
}
