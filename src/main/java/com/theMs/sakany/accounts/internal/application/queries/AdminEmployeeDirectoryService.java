package com.theMs.sakany.accounts.internal.application.queries;

import com.theMs.sakany.accounts.internal.domain.EmployeeAccountStatus;
import com.theMs.sakany.accounts.internal.domain.LoginMethod;
import com.theMs.sakany.accounts.internal.domain.Role;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.AdminProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.AdminProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.TechnicianProfileEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.TechnicianProfileJpaRepository;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserEntity;
import com.theMs.sakany.accounts.internal.infrastructure.persistence.UserJpaRepository;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminEmployeeDirectoryService {

    private static final List<Role> STAFF_ROLES = List.of(
            Role.ADMIN,
            Role.TECHNICIAN,
            Role.SECURITY_GUARD
    );

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String SUPER_ADMIN_PERMISSION = "SUPER_ADMIN";
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final UserJpaRepository userJpaRepository;
    private final AdminProfileJpaRepository adminProfileJpaRepository;
    private final TechnicianProfileJpaRepository technicianProfileJpaRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AdminEmployeeDirectoryService(
            UserJpaRepository userJpaRepository,
            AdminProfileJpaRepository adminProfileJpaRepository,
            TechnicianProfileJpaRepository technicianProfileJpaRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userJpaRepository = userJpaRepository;
        this.adminProfileJpaRepository = adminProfileJpaRepository;
        this.technicianProfileJpaRepository = technicianProfileJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminEmployeesDashboardResponse getEmployees(
            String search,
            AdminEmployeeRoleFilter role,
            AdminEmployeeStatusFilter status,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        String normalizedSearch = normalize(search);
        AdminEmployeeRoleFilter effectiveRole = role == null ? AdminEmployeeRoleFilter.ALL : role;
        AdminEmployeeStatusFilter effectiveStatus = status == null ? AdminEmployeeStatusFilter.ALL : status;

        List<UserEntity> candidates = userJpaRepository.findStaffForAdmin(
                normalizedSearch,
                effectiveStatus.name(),
                STAFF_ROLES
        );

        ProfileSnapshot profileSnapshot = loadProfiles(candidates);
        Map<UUID, AdminProfileEntity> adminProfileByUserId = profileSnapshot.adminProfileByUserId();

        List<UserEntity> filtered = candidates.stream()
                .filter(user -> matchesRoleFilter(user, effectiveRole, adminProfileByUserId))
                .toList();

        long totalElements = filtered.size();
        int fromIndex = Math.min(safePage * safeSize, filtered.size());
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());

        List<AdminEmployeeItem> items = filtered.subList(fromIndex, toIndex)
                .stream()
                .map(user -> mapItem(user, profileSnapshot))
                .toList();

        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        return new AdminEmployeesDashboardResponse(
                items,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                safePage + 1 < totalPages,
                safePage > 0 && totalPages > 0,
                buildSummary()
        );
    }

    public AdminEmployeeItem getEmployee(UUID employeeId) {
        UserEntity user = getStaffEmployee(employeeId);
        ProfileSnapshot profileSnapshot = loadProfiles(List.of(user));
        return mapItem(user, profileSnapshot);
    }

    public List<String> getRoleOptions() {
        return ArraysAsList(
                AdminEmployeeRoleFilter.ALL.name(),
                AdminEmployeeRoleFilter.SUPER_ADMIN.name(),
                AdminEmployeeRoleFilter.ADMIN.name(),
                AdminEmployeeRoleFilter.TECHNICIAN.name(),
                AdminEmployeeRoleFilter.SECURITY_STAFF.name()
        );
    }

    public List<String> getStatusOptions() {
        return ArraysAsList(
                AdminEmployeeStatusFilter.ALL.name(),
                AdminEmployeeStatusFilter.ACTIVE.name(),
                AdminEmployeeStatusFilter.INACTIVE.name(),
                AdminEmployeeStatusFilter.SUSPENDED.name()
        );
    }

    @Transactional
    public CreateEmployeeResult createEmployee(CreateEmployeeRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        NameParts nameParts = resolveNameParts(request.fullName(), request.firstName(), request.lastName());
        String normalizedPhone = requireNonBlank(request.phoneNumber(), "phoneNumber is required");
        String normalizedEmail = normalizeEmail(requireNonBlank(request.email(), "email is required"));
        LoginMethod loginMethod = request.loginMethod() == null ? LoginMethod.EMAIL_PASSWORD : request.loginMethod();

        RoleSelection roleSelection = resolveRoleSelection(request.role(), request.isSuperAdmin(), false);
        EmployeeAccountStatus accountStatus = resolveAccountStatus(request.isActive(), null);
        boolean phoneVerified = Boolean.TRUE.equals(request.isPhoneVerified());

        if (request.hireDate() == null) {
            throw new BusinessRuleException("hireDate is required");
        }

        validatePassword(request.password(), request.confirmPassword(), loginMethod, true, true);
        validateUserUniqueness(null, normalizedEmail, normalizedPhone);

        UserEntity user = new UserEntity(
                nameParts.firstName(),
                nameParts.lastName(),
                normalizedPhone,
                normalizedEmail,
                request.password() == null ? null : passwordEncoder.encode(request.password()),
                roleSelection.role(),
                accountStatus == EmployeeAccountStatus.ACTIVE,
                phoneVerified,
                loginMethod
        );
        user.setId(UUID.randomUUID());

        user.setEmploymentStatus(accountStatus);
        user.setHireDate(request.hireDate());
        user.setDepartment(normalize(request.department()));

        UserEntity saved = userJpaRepository.save(user);

        syncProfiles(
                saved,
                roleSelection,
                request.scopePermissions(),
                request.specializations(),
                request.technicianAvailable()
        );

        return new CreateEmployeeResult(saved.getId(), "Employee created successfully");
    }

    @Transactional
    public void updateEmployee(UUID employeeId, UpdateEmployeeRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        UserEntity user = getStaffEmployee(employeeId);

        if (request.fullName() != null) {
            NameParts parts = splitFullName(request.fullName());
            user.setFirstName(parts.firstName());
            user.setLastName(parts.lastName());
        }

        if (request.firstName() != null) {
            user.setFirstName(requireNonBlank(request.firstName(), "firstName cannot be blank"));
        }

        if (request.lastName() != null) {
            user.setLastName(requireNonBlank(request.lastName(), "lastName cannot be blank"));
        }

        if (request.phoneNumber() != null) {
            String normalizedPhone = requireNonBlank(request.phoneNumber(), "phoneNumber cannot be blank");
            validateUserUniqueness(user.getId(), null, normalizedPhone);
            user.setPhone(normalizedPhone);
        }

        if (request.email() != null) {
            String normalizedEmail = normalizeEmail(request.email());
            validateUserUniqueness(user.getId(), normalizedEmail, null);
            user.setEmail(normalizedEmail);
        }

        if (request.password() != null) {
            LoginMethod effectiveLoginMethod = request.loginMethod() == null ? user.getAuthProvider() : request.loginMethod();
            validatePassword(request.password(), request.confirmPassword(), effectiveLoginMethod, false, false);
            if (!request.password().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.password()));
            }
        }

        if (request.loginMethod() != null) {
            user.setAuthProvider(request.loginMethod());
        }

        if (request.hireDate() != null) {
            user.setHireDate(request.hireDate());
        }

        if (request.department() != null) {
            user.setDepartment(normalize(request.department()));
        }

        if (request.isPhoneVerified() != null) {
            user.setPhoneVerified(request.isPhoneVerified());
        }

        RoleSelection currentRoleSelection = resolveCurrentRoleSelection(user);
        RoleSelection targetRoleSelection = currentRoleSelection;

        if (request.role() != null || request.isSuperAdmin() != null) {
            targetRoleSelection = resolveRoleSelection(
                    request.role() == null ? currentRoleSelection.filter().name() : request.role(),
                    request.isSuperAdmin(),
                    false
            );
            user.setRole(targetRoleSelection.role());
        }

        if (request.isActive() != null) {
            user.setEmploymentStatus(request.isActive() ? EmployeeAccountStatus.ACTIVE : EmployeeAccountStatus.INACTIVE);
        }

        userJpaRepository.save(user);

        syncProfiles(
                user,
                targetRoleSelection,
                request.scopePermissions(),
                request.specializations(),
                request.technicianAvailable()
        );
    }

    @Transactional
    public void updateStatus(UUID employeeId, UpdateEmployeeStatusRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        UserEntity user = getStaffEmployee(employeeId);
        EmployeeAccountStatus accountStatus = resolveAccountStatus(request.isActive(), request.status());
        user.setEmploymentStatus(accountStatus);
        userJpaRepository.save(user);
    }

    @Transactional
    public ResetPasswordResult resetPassword(UUID employeeId, String newPassword) {
        UserEntity user = getStaffEmployee(employeeId);

        String effectivePassword = normalize(newPassword);
        if (effectivePassword == null) {
            effectivePassword = generateTempPassword(10);
        }

        if (effectivePassword.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(effectivePassword));
        userJpaRepository.save(user);

        return new ResetPasswordResult(user.getId(), "Password reset successfully", effectivePassword);
    }

    @Transactional
    public void deactivateEmployee(UUID employeeId) {
        UserEntity user = getStaffEmployee(employeeId);
        user.setEmploymentStatus(EmployeeAccountStatus.INACTIVE);
        userJpaRepository.save(user);
    }

    private AdminEmployeesSummary buildSummary() {
        List<UserEntity> allEmployees = userJpaRepository.findAllByRoleIn(STAFF_ROLES);
        ProfileSnapshot profileSnapshot = loadProfiles(allEmployees);

        long superAdminsCount = 0;
        long adminsCount = 0;
        long techniciansCount = 0;
        long securityStaffCount = 0;
        long activeEmployeesCount = 0;

        for (UserEntity user : allEmployees) {
            AdminProfileEntity adminProfile = profileSnapshot.adminProfileByUserId().get(user.getId());
            boolean isSuperAdmin = isSuperAdmin(adminProfile);

            if (user.getRole() == Role.ADMIN) {
                if (isSuperAdmin) {
                    superAdminsCount++;
                } else {
                    adminsCount++;
                }
            } else if (user.getRole() == Role.TECHNICIAN) {
                techniciansCount++;
            } else if (user.getRole() == Role.SECURITY_GUARD) {
                securityStaffCount++;
            }

            if (deriveAccountStatus(user) == EmployeeAccountStatus.ACTIVE) {
                activeEmployeesCount++;
            }
        }

        long totalEmployeesCount = allEmployees.size();
        return new AdminEmployeesSummary(
                superAdminsCount,
                adminsCount,
                techniciansCount,
                securityStaffCount,
                activeEmployeesCount,
                totalEmployeesCount,
                activeEmployeesCount + "/" + totalEmployeesCount
        );
    }

    private AdminEmployeeItem mapItem(UserEntity user, ProfileSnapshot profileSnapshot) {
        AdminProfileEntity adminProfile = profileSnapshot.adminProfileByUserId().get(user.getId());
        TechnicianProfileEntity technicianProfile = profileSnapshot.technicianProfileByUserId().get(user.getId());

        boolean isSuperAdmin = isSuperAdmin(adminProfile);
        AdminEmployeeRoleFilter role = toRoleFilter(user.getRole(), isSuperAdmin);
        EmployeeAccountStatus accountStatus = deriveAccountStatus(user);

        return new AdminEmployeeItem(
                user.getId(),
                formatFullName(user.getFirstName(), user.getLastName()),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                role.name(),
                toRoleLabel(role),
                user.getDepartment(),
                accountStatus.name(),
                toStatusLabel(accountStatus),
                user.getUpdatedAt(),
                user.getHireDate(),
                isSuperAdmin,
                accountStatus == EmployeeAccountStatus.ACTIVE,
                user.isPhoneVerified(),
                user.getAuthProvider() == null ? null : user.getAuthProvider().name(),
                technicianProfile == null || technicianProfile.getSpecializations() == null
                        ? List.of()
                        : List.copyOf(technicianProfile.getSpecializations()),
                technicianProfile == null ? null : technicianProfile.isAvailable(),
                buildActions(user.getId())
        );
    }

    private AdminEmployeeActions buildActions(UUID employeeId) {
        String base = "/v1/admin/employees/" + employeeId;
        return new AdminEmployeeActions(
                base,
                base,
                base + "/status",
                base + "/reset-password",
                base
        );
    }

    private ProfileSnapshot loadProfiles(Collection<UserEntity> users) {
        if (users == null || users.isEmpty()) {
            return new ProfileSnapshot(Map.of(), Map.of());
        }

        List<UUID> userIds = users.stream().map(UserEntity::getId).toList();

        Map<UUID, AdminProfileEntity> adminByUserId = adminProfileJpaRepository.findByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        Map<UUID, TechnicianProfileEntity> technicianByUserId = technicianProfileJpaRepository.findByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));

        return new ProfileSnapshot(adminByUserId, technicianByUserId);
    }

    private UserEntity getStaffEmployee(UUID employeeId) {
        UserEntity user = userJpaRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee", employeeId));

        if (!STAFF_ROLES.contains(user.getRole())) {
            throw new BusinessRuleException("Provided user is not an employee");
        }

        return user;
    }

    private void syncProfiles(
            UserEntity user,
            RoleSelection roleSelection,
            List<String> requestedScopePermissions,
            List<String> requestedSpecializations,
            Boolean technicianAvailable
    ) {
        if (roleSelection.role() == Role.ADMIN) {
            technicianProfileJpaRepository.deleteByUserId(user.getId());
            upsertAdminProfile(user, roleSelection.isSuperAdmin(), requestedScopePermissions);
            return;
        }

        adminProfileJpaRepository.deleteByUserId(user.getId());

        if (roleSelection.role() == Role.TECHNICIAN) {
            upsertTechnicianProfile(user, requestedSpecializations, technicianAvailable);
            return;
        }

        technicianProfileJpaRepository.deleteByUserId(user.getId());
    }

    private void upsertAdminProfile(UserEntity user, boolean isSuperAdmin, List<String> requestedScopePermissions) {
        Optional<AdminProfileEntity> existing = adminProfileJpaRepository.findFirstByUserId(user.getId());

        List<String> basePermissions = requestedScopePermissions != null
                ? normalizeList(requestedScopePermissions)
                : existing.map(AdminProfileEntity::getScopePermissions).map(this::normalizeList).orElseGet(ArrayList::new);

        LinkedHashSet<String> permissionSet = new LinkedHashSet<>(basePermissions);
        if (isSuperAdmin) {
            permissionSet.add(SUPER_ADMIN_PERMISSION);
        } else {
            permissionSet.removeIf(value -> SUPER_ADMIN_PERMISSION.equalsIgnoreCase(value));
        }

        AdminProfileEntity adminProfile = existing.orElseGet(() -> {
            AdminProfileEntity entity = new AdminProfileEntity();
            entity.setId(UUID.randomUUID());
            return entity;
        });
        adminProfile.setUser(user);
        adminProfile.setScopePermissions(new ArrayList<>(permissionSet));
        adminProfileJpaRepository.save(adminProfile);
    }

    private void upsertTechnicianProfile(UserEntity user, List<String> requestedSpecializations, Boolean technicianAvailable) {
        Optional<TechnicianProfileEntity> existing = technicianProfileJpaRepository.findByUserId(user.getId())
                .stream()
                .findFirst();

        List<String> specializations = requestedSpecializations != null
                ? normalizeList(requestedSpecializations)
                : existing.map(TechnicianProfileEntity::getSpecializations).map(this::normalizeList).orElseGet(ArrayList::new);

        TechnicianProfileEntity technicianProfile = existing.orElseGet(() -> {
            TechnicianProfileEntity entity = new TechnicianProfileEntity();
            entity.setId(UUID.randomUUID());
            return entity;
        });
        technicianProfile.setUser(user);
        technicianProfile.setSpecializations(specializations);
        technicianProfile.setAvailable(technicianAvailable != null
                ? technicianAvailable
                : existing.map(TechnicianProfileEntity::isAvailable).orElse(true));

        technicianProfileJpaRepository.save(technicianProfile);
    }

    private RoleSelection resolveCurrentRoleSelection(UserEntity user) {
        if (user.getRole() != Role.ADMIN) {
            return new RoleSelection(toRoleFilter(user.getRole(), false), user.getRole(), false);
        }

        boolean isSuperAdmin = adminProfileJpaRepository.findFirstByUserId(user.getId())
                .map(this::isSuperAdmin)
                .orElse(false);

        return new RoleSelection(
                isSuperAdmin ? AdminEmployeeRoleFilter.SUPER_ADMIN : AdminEmployeeRoleFilter.ADMIN,
                Role.ADMIN,
                isSuperAdmin
        );
    }

    private RoleSelection resolveRoleSelection(String rawRole, Boolean isSuperAdminFlag, boolean defaultToAdmin) {
        AdminEmployeeRoleFilter roleFilter;
        if (rawRole == null || rawRole.isBlank()) {
            roleFilter = defaultToAdmin ? AdminEmployeeRoleFilter.ADMIN : AdminEmployeeRoleFilter.ALL;
        } else {
            try {
                roleFilter = AdminEmployeeRoleFilter.from(rawRole);
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException(ex.getMessage());
            }
        }

        if (roleFilter == AdminEmployeeRoleFilter.ALL) {
            throw new BusinessRuleException("role is required");
        }

        return switch (roleFilter) {
            case SUPER_ADMIN -> new RoleSelection(roleFilter, Role.ADMIN, true);
            case ADMIN -> new RoleSelection(roleFilter, Role.ADMIN, Boolean.TRUE.equals(isSuperAdminFlag));
            case TECHNICIAN -> new RoleSelection(roleFilter, Role.TECHNICIAN, false);
            case SECURITY_STAFF -> new RoleSelection(roleFilter, Role.SECURITY_GUARD, false);
            default -> throw new BusinessRuleException("Unsupported role");
        };
    }

    private boolean matchesRoleFilter(
            UserEntity user,
            AdminEmployeeRoleFilter roleFilter,
            Map<UUID, AdminProfileEntity> adminProfileByUserId
    ) {
        if (roleFilter == null || roleFilter == AdminEmployeeRoleFilter.ALL) {
            return true;
        }

        AdminProfileEntity adminProfile = adminProfileByUserId.get(user.getId());
        boolean isSuperAdmin = isSuperAdmin(adminProfile);

        return switch (roleFilter) {
            case SUPER_ADMIN -> user.getRole() == Role.ADMIN && isSuperAdmin;
            case ADMIN -> user.getRole() == Role.ADMIN && !isSuperAdmin;
            case TECHNICIAN -> user.getRole() == Role.TECHNICIAN;
            case SECURITY_STAFF -> user.getRole() == Role.SECURITY_GUARD;
            case ALL -> true;
        };
    }

    private AdminEmployeeRoleFilter toRoleFilter(Role role, boolean isSuperAdmin) {
        if (role == Role.ADMIN) {
            return isSuperAdmin ? AdminEmployeeRoleFilter.SUPER_ADMIN : AdminEmployeeRoleFilter.ADMIN;
        }
        if (role == Role.TECHNICIAN) {
            return AdminEmployeeRoleFilter.TECHNICIAN;
        }
        if (role == Role.SECURITY_GUARD) {
            return AdminEmployeeRoleFilter.SECURITY_STAFF;
        }

        throw new BusinessRuleException("Unsupported employee role: " + role);
    }

    private EmployeeAccountStatus resolveAccountStatus(Boolean isActive, String statusRawValue) {
        if (statusRawValue != null && !statusRawValue.isBlank()) {
            AdminEmployeeStatusFilter statusFilter;
            try {
                statusFilter = AdminEmployeeStatusFilter.from(statusRawValue);
            } catch (IllegalArgumentException ex) {
                throw new BusinessRuleException(ex.getMessage());
            }
            if (statusFilter == AdminEmployeeStatusFilter.ALL) {
                throw new BusinessRuleException("status must be ACTIVE, INACTIVE, or SUSPENDED");
            }
            return EmployeeAccountStatus.valueOf(statusFilter.name());
        }

        if (isActive == null) {
            return EmployeeAccountStatus.ACTIVE;
        }

        return isActive ? EmployeeAccountStatus.ACTIVE : EmployeeAccountStatus.INACTIVE;
    }

    private EmployeeAccountStatus deriveAccountStatus(UserEntity user) {
        EmployeeAccountStatus status = user.getEmploymentStatus();
        if (status == null) {
            return user.isActive() ? EmployeeAccountStatus.ACTIVE : EmployeeAccountStatus.INACTIVE;
        }
        return status;
    }

    private boolean isSuperAdmin(AdminProfileEntity adminProfileEntity) {
        if (adminProfileEntity == null || adminProfileEntity.getScopePermissions() == null) {
            return false;
        }

        return adminProfileEntity.getScopePermissions().stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .anyMatch(SUPER_ADMIN_PERMISSION::equals);
    }

    private String formatFullName(String firstName, String lastName) {
        return (safe(firstName) + " " + safe(lastName)).trim();
    }

    private String toRoleLabel(AdminEmployeeRoleFilter roleFilter) {
        return switch (roleFilter) {
            case ALL -> "All";
            case SUPER_ADMIN -> "Super Admin";
            case ADMIN -> "Admin";
            case TECHNICIAN -> "Technician";
            case SECURITY_STAFF -> "Security Staff";
        };
    }

    private String toStatusLabel(EmployeeAccountStatus status) {
        return switch (status) {
            case ACTIVE -> "Active";
            case INACTIVE -> "Inactive";
            case SUSPENDED -> "Suspended";
        };
    }

    private NameParts resolveNameParts(String fullName, String firstName, String lastName) {
        if (fullName != null && !fullName.isBlank()) {
            return splitFullName(fullName);
        }

        String safeFirst = normalize(firstName);
        String safeLast = normalize(lastName);

        if (safeFirst == null || safeLast == null) {
            throw new BusinessRuleException("firstName and lastName are required");
        }

        return new NameParts(safeFirst, safeLast);
    }

    private NameParts splitFullName(String fullName) {
        String safeFullName = normalize(fullName);
        if (safeFullName == null) {
            throw new BusinessRuleException("fullName cannot be blank");
        }

        String[] parts = safeFullName.split("\\s+");
        if (parts.length < 2) {
            throw new BusinessRuleException("fullName must include first and last name");
        }

        String firstName = parts[0];
        String lastName = String.join(" ", List.of(parts).subList(1, parts.length));
        return new NameParts(firstName, lastName);
    }

    private void validateUserUniqueness(UUID userId, String email, String phoneNumber) {
        if (email != null) {
            userJpaRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new BusinessRuleException("Email already registered");
                }
            });
        }

        if (phoneNumber != null) {
            userJpaRepository.findByPhone(phoneNumber).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new BusinessRuleException("Phone number already registered");
                }
            });
        }
    }

    private void validatePassword(
            String password,
            String confirmPassword,
            LoginMethod loginMethod,
            boolean requiredWhenEmailPassword,
            boolean requireConfirmWhenPasswordProvided
    ) {
        if (loginMethod == LoginMethod.EMAIL_PASSWORD && requiredWhenEmailPassword && (password == null || password.isBlank())) {
            throw new BusinessRuleException("password is required when loginMethod is EMAIL_PASSWORD");
        }

        if (password == null || password.isBlank()) {
            return;
        }

        if (requireConfirmWhenPasswordProvided && (confirmPassword == null || confirmPassword.isBlank())) {
            throw new BusinessRuleException("confirmPassword is required");
        }

        if (password.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters");
        }

        if (confirmPassword != null && !password.equals(confirmPassword)) {
            throw new BusinessRuleException("password and confirmPassword do not match");
        }
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null) {
            return null;
        }

        String lowered = normalizedEmail.toLowerCase(Locale.ROOT);
        if (!lowered.contains("@")) {
            throw new BusinessRuleException("Invalid email format");
        }

        return lowered;
    }

    private String generateTempPassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(TEMP_PASSWORD_CHARS.length());
            builder.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return builder.toString();
    }

    private List<String> normalizeList(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String safeValue = normalize(value);
            if (safeValue != null) {
                normalized.add(safeValue);
            }
        }

        return new ArrayList<>(normalized);
    }

    private String requireNonBlank(String value, String errorMessage) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new BusinessRuleException(errorMessage);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private List<String> ArraysAsList(String... values) {
        return List.of(values);
    }

    public record AdminEmployeesDashboardResponse(
            List<AdminEmployeeItem> employees,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            AdminEmployeesSummary summary
    ) {
    }

    public record AdminEmployeeItem(
            UUID employeeId,
            String fullName,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String role,
            String roleLabel,
            String department,
            String status,
            String statusLabel,
            Instant lastActiveAt,
            LocalDate hireDate,
            boolean superAdmin,
            boolean active,
            boolean phoneVerified,
            String loginMethod,
            List<String> specializations,
            Boolean technicianAvailable,
            AdminEmployeeActions actions
    ) {
    }

    public record AdminEmployeeActions(
            String viewUrl,
            String editUrl,
            String updateStatusUrl,
            String resetPasswordUrl,
            String deactivateUrl
    ) {
    }

    public record AdminEmployeesSummary(
            long superAdminsCount,
            long adminsCount,
            long techniciansCount,
            long securityStaffCount,
            long activeEmployeesCount,
            long totalEmployeesCount,
            String activeEmployeesLabel
    ) {
    }

    public record CreateEmployeeRequest(
            String fullName,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String password,
            String confirmPassword,
            String role,
            LocalDate hireDate,
            String department,
            Boolean isSuperAdmin,
            Boolean isActive,
            Boolean isPhoneVerified,
            LoginMethod loginMethod,
            List<String> scopePermissions,
            List<String> specializations,
            Boolean technicianAvailable
    ) {
    }

    public record UpdateEmployeeRequest(
            String fullName,
            String firstName,
            String lastName,
            String phoneNumber,
            String email,
            String password,
            String confirmPassword,
            String role,
            LocalDate hireDate,
            String department,
            Boolean isSuperAdmin,
            Boolean isActive,
            Boolean isPhoneVerified,
            LoginMethod loginMethod,
            List<String> scopePermissions,
            List<String> specializations,
            Boolean technicianAvailable
    ) {
    }

    public record UpdateEmployeeStatusRequest(
            Boolean isActive,
            String status
    ) {
    }

    public record CreateEmployeeResult(
            UUID employeeId,
            String message
    ) {
    }

    public record ResetPasswordResult(
            UUID employeeId,
            String message,
            String temporaryPassword
    ) {
    }

    private record RoleSelection(
            AdminEmployeeRoleFilter filter,
            Role role,
            boolean isSuperAdmin
    ) {
    }

    private record ProfileSnapshot(
            Map<UUID, AdminProfileEntity> adminProfileByUserId,
            Map<UUID, TechnicianProfileEntity> technicianProfileByUserId
    ) {
    }

    private record NameParts(
            String firstName,
            String lastName
    ) {
    }
}
