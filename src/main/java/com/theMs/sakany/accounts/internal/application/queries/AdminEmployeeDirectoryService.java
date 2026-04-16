package com.theMs.sakany.accounts.internal.application.queries;

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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminEmployeeDirectoryService {

    private static final List<Role> STAFF_ROLES = List.of(Role.ADMIN, Role.TECHNICIAN, Role.SECURITY_GUARD);
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 100;
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
        AdminEmployeeRoleFilter roleFilter = role == null ? AdminEmployeeRoleFilter.ALL : role;
        String statusFilter = resolveStatusFilter(status);
        String normalizedSearch = normalizeSearch(search);

        List<UserEntity> users = userJpaRepository.findStaffForAdmin(
                normalizedSearch,
                statusFilter,
                resolveDbRoles(roleFilter)
        );

        Map<UUID, AdminProfileEntity> adminProfiles = loadAdminProfiles(users);
        Map<UUID, TechnicianProfileEntity> technicianProfiles = loadTechnicianProfiles(users);

        List<AdminEmployeeItem> filteredItems = users.stream()
                .map(user -> mapEmployee(user, adminProfiles.get(user.getId()), technicianProfiles.get(user.getId())))
                .filter(item -> matchesRoleFilter(item.roleCode(), roleFilter))
                .toList();

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        int fromIndex = Math.min(safePage * safeSize, filteredItems.size());
        int toIndex = Math.min(fromIndex + safeSize, filteredItems.size());
        List<AdminEmployeeItem> pageItems = filteredItems.subList(fromIndex, toIndex);

        long totalElements = filteredItems.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        boolean hasNext = safePage + 1 < totalPages;
        boolean hasPrevious = safePage > 0 && totalElements > 0;

        return new AdminEmployeesDashboardResponse(
                pageItems,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious,
                buildSummary(),
                getRoleOptions(),
                getStatusOptions()
        );
    }

    public AdminEmployeeItem getEmployee(UUID employeeId) {
        UserEntity employee = findStaffUser(employeeId);
        AdminProfileEntity adminProfile = findFirstAdminProfile(employeeId);
        TechnicianProfileEntity technicianProfile = findFirstTechnicianProfile(employeeId);
        return mapEmployee(employee, adminProfile, technicianProfile);
    }

    public List<String> getRoleOptions() {
        return Arrays.stream(AdminEmployeeRoleFilter.values()).map(Enum::name).toList();
    }

    public List<String> getStatusOptions() {
        return Arrays.stream(AdminEmployeeStatusFilter.values()).map(Enum::name).toList();
    }

    @Transactional
    public CreateEmployeeResult createEmployee(CreateEmployeeRequest request) {
        validateCreateRequest(request);

        NameParts nameParts = resolveNameParts(request.fullName(), request.firstName(), request.lastName());
        String firstName = nameParts.firstName();
        String lastName = nameParts.lastName();
        String phoneNumber = normalizePhone(request.phoneNumber());
        String email = normalizeEmail(request.email());
        LoginMethod loginMethod = request.loginMethod() == null ? LoginMethod.EMAIL_PASSWORD : request.loginMethod();

        ensureUniquePhone(phoneNumber, null);
        ensureUniqueEmail(email, null);

        RoleSelection roleSelection = resolveRoleForCreate(request.role(), request.isSuperAdmin());

        String rawPassword = request.password();
        String temporaryPassword = null;
        if (loginMethod == LoginMethod.EMAIL_PASSWORD) {
            if (request.confirmPassword() != null && rawPassword == null) {
                throw new BusinessRuleException("password is required when confirmPassword is provided");
            }
            if (rawPassword != null && request.confirmPassword() != null && !rawPassword.equals(request.confirmPassword())) {
                throw new BusinessRuleException("password and confirmPassword do not match");
            }
            if (isBlank(rawPassword)) {
                rawPassword = generateTempPassword(10);
                temporaryPassword = rawPassword;
            }
            if (rawPassword.length() < 8) {
                throw new BusinessRuleException("Password must be at least 8 characters");
            }
        }

        String hashedPassword = isBlank(rawPassword) ? null : passwordEncoder.encode(rawPassword);

        UserEntity employee = new UserEntity(
                firstName,
                lastName,
                phoneNumber,
                email,
                hashedPassword,
                roleSelection.databaseRole(),
                request.isActive() == null || request.isActive(),
                Boolean.TRUE.equals(request.isPhoneVerified()),
                loginMethod
        );
        employee.setId(UUID.randomUUID());
        employee.setHireDate(request.hireDate());
        employee.setDepartment(resolveDepartmentValue(request.department()));

        UserEntity savedEmployee = userJpaRepository.save(employee);

        ProfileSnapshot profiles = synchronizeProfiles(
                savedEmployee,
                roleSelection,
                request.scopePermissions(),
                request.specializations(),
                request.technicianAvailable(),
                null,
                null
        );

        return new CreateEmployeeResult(
                savedEmployee.getId(),
                "Employee created successfully",
                temporaryPassword,
                mapEmployee(savedEmployee, profiles.adminProfile(), profiles.technicianProfile())
        );
    }

    @Transactional
    public void updateEmployee(UUID employeeId, UpdateEmployeeRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        UserEntity employee = findStaffUser(employeeId);
        AdminProfileEntity existingAdminProfile = findFirstAdminProfile(employeeId);
        TechnicianProfileEntity existingTechnicianProfile = findFirstTechnicianProfile(employeeId);

        if (request.firstName() != null) {
            if (request.firstName().isBlank()) {
                throw new BusinessRuleException("firstName cannot be blank");
            }
            employee.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            if (request.lastName().isBlank()) {
                throw new BusinessRuleException("lastName cannot be blank");
            }
            employee.setLastName(request.lastName().trim());
        }
        if (request.fullName() != null) {
            NameParts nameParts = resolveNameParts(request.fullName(), null, null);
            employee.setFirstName(nameParts.firstName());
            employee.setLastName(nameParts.lastName());
        }
        if (request.phoneNumber() != null) {
            String normalizedPhone = normalizePhone(request.phoneNumber());
            ensureUniquePhone(normalizedPhone, employeeId);
            employee.setPhone(normalizedPhone);
        }
        if (request.email() != null) {
            String normalizedEmail = normalizeEmail(request.email());
            ensureUniqueEmail(normalizedEmail, employeeId);
            employee.setEmail(normalizedEmail);
        }
        if (request.isActive() != null) {
            employee.setActive(request.isActive());
        }
        if (request.isPhoneVerified() != null) {
            employee.setPhoneVerified(request.isPhoneVerified());
        }
        if (request.loginMethod() != null) {
            employee.setAuthProvider(request.loginMethod());
        }
        if (request.password() != null) {
            if (request.password().isBlank() || request.password().length() < 8) {
                throw new BusinessRuleException("Password must be at least 8 characters");
            }
            if (request.confirmPassword() != null && !request.password().equals(request.confirmPassword())) {
                throw new BusinessRuleException("password and confirmPassword do not match");
            }
            employee.setPasswordHash(passwordEncoder.encode(request.password()));
            if (employee.getAuthProvider() != LoginMethod.EMAIL_PASSWORD) {
                employee.setAuthProvider(LoginMethod.EMAIL_PASSWORD);
            }
        }
        if (request.hireDate() != null) {
            employee.setHireDate(request.hireDate());
        }
        if (request.department() != null) {
            employee.setDepartment(resolveDepartmentValue(request.department()));
        }

        RoleSelection roleSelection = resolveRoleForUpdate(
                employee.getRole(),
                existingAdminProfile,
                request.role(),
                request.isSuperAdmin()
        );
        employee.setRole(roleSelection.databaseRole());

        UserEntity savedEmployee = userJpaRepository.save(employee);
        synchronizeProfiles(
                savedEmployee,
                roleSelection,
                request.scopePermissions(),
                request.specializations(),
                request.technicianAvailable(),
                existingAdminProfile,
                existingTechnicianProfile
        );
    }

    @Transactional
    public void updateStatus(UUID employeeId, UpdateEmployeeStatusRequest request) {
        if (request == null || request.isActive() == null) {
            throw new BusinessRuleException("isActive is required");
        }

        UserEntity employee = findStaffUser(employeeId);
        employee.setActive(request.isActive());
        userJpaRepository.save(employee);
    }

    @Transactional
    public ResetPasswordResult resetPassword(UUID employeeId, String newPassword) {
        UserEntity employee = findStaffUser(employeeId);

        String rawPassword = isBlank(newPassword) ? generateTempPassword(10) : newPassword.trim();
            if (rawPassword == null || rawPassword.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters");
        }

        employee.setPasswordHash(passwordEncoder.encode(rawPassword));
        if (employee.getAuthProvider() != LoginMethod.EMAIL_PASSWORD) {
            employee.setAuthProvider(LoginMethod.EMAIL_PASSWORD);
        }
        userJpaRepository.save(employee);

        return new ResetPasswordResult(
                employeeId,
                "Password reset successfully",
                rawPassword
        );
    }

    @Transactional
    public void deactivateEmployee(UUID employeeId) {
        UserEntity employee = findStaffUser(employeeId);
        if (employee.isActive()) {
            employee.setActive(false);
            userJpaRepository.save(employee);
        }
    }

    private AdminEmployeesSummary buildSummary() {
        List<UserEntity> allStaff = userJpaRepository.findAllByRoleIn(STAFF_ROLES);
        Map<UUID, AdminProfileEntity> adminProfiles = loadAdminProfiles(allStaff);

        long superAdmins = 0;
        long admins = 0;
        long technicians = 0;
        long securityStaff = 0;
        long activeEmployees = allStaff.stream().filter(UserEntity::isActive).count();

        for (UserEntity employee : allStaff) {
            String roleCode = resolveRoleCode(employee, adminProfiles.get(employee.getId()));
            if (AdminEmployeeRoleFilter.SUPER_ADMIN.name().equals(roleCode)) {
                superAdmins++;
            } else if (AdminEmployeeRoleFilter.ADMIN.name().equals(roleCode)) {
                admins++;
            } else if (AdminEmployeeRoleFilter.TECHNICIAN.name().equals(roleCode)) {
                technicians++;
            } else if (AdminEmployeeRoleFilter.SECURITY_STAFF.name().equals(roleCode)) {
                securityStaff++;
            }
        }

        long totalEmployees = allStaff.size();

        return new AdminEmployeesSummary(
                superAdmins,
                admins,
                technicians,
                securityStaff,
                activeEmployees,
                totalEmployees,
                activeEmployees + "/" + totalEmployees
        );
    }

    private UserEntity findStaffUser(UUID employeeId) {
        UserEntity employee = userJpaRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee", employeeId));

        if (!STAFF_ROLES.contains(employee.getRole())) {
            throw new NotFoundException("Employee", employeeId);
        }

        return employee;
    }

    private Map<UUID, AdminProfileEntity> loadAdminProfiles(List<UserEntity> users) {
        List<UUID> adminUserIds = users.stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .map(UserEntity::getId)
                .toList();

        if (adminUserIds.isEmpty()) {
            return Map.of();
        }

        return adminProfileJpaRepository.findByUserIds(adminUserIds).stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        profile -> profile,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private Map<UUID, TechnicianProfileEntity> loadTechnicianProfiles(List<UserEntity> users) {
        List<UUID> technicianUserIds = users.stream()
                .filter(user -> user.getRole() == Role.TECHNICIAN)
                .map(UserEntity::getId)
                .toList();

        if (technicianUserIds.isEmpty()) {
            return Map.of();
        }

        return technicianProfileJpaRepository.findByUserIds(technicianUserIds).stream()
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        profile -> profile,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private AdminProfileEntity findFirstAdminProfile(UUID userId) {
        return adminProfileJpaRepository.findByUserId(userId).stream().findFirst().orElse(null);
    }

    private TechnicianProfileEntity findFirstTechnicianProfile(UUID userId) {
        return technicianProfileJpaRepository.findByUserId(userId).stream().findFirst().orElse(null);
    }

    private ProfileSnapshot synchronizeProfiles(
            UserEntity employee,
            RoleSelection roleSelection,
            List<String> requestedPermissions,
            List<String> requestedSpecializations,
            Boolean requestedTechnicianAvailable,
            AdminProfileEntity existingAdminProfile,
            TechnicianProfileEntity existingTechnicianProfile
    ) {
        adminProfileJpaRepository.deleteByUserId(employee.getId());
        technicianProfileJpaRepository.deleteByUserId(employee.getId());

        AdminProfileEntity savedAdminProfile = null;
        TechnicianProfileEntity savedTechnicianProfile = null;

        if (roleSelection.databaseRole() == Role.ADMIN) {
            List<String> scopePermissions = resolveScopePermissions(
                    roleSelection.superAdmin(),
                    requestedPermissions,
                    existingAdminProfile
            );

            savedAdminProfile = new AdminProfileEntity(employee, scopePermissions);
            savedAdminProfile.setId(UUID.randomUUID());
            savedAdminProfile = adminProfileJpaRepository.save(savedAdminProfile);
        }

        if (roleSelection.databaseRole() == Role.TECHNICIAN) {
            List<String> specializations = resolveSpecializations(requestedSpecializations, existingTechnicianProfile);
            boolean isAvailable = requestedTechnicianAvailable != null
                    ? requestedTechnicianAvailable
                    : existingTechnicianProfile == null || existingTechnicianProfile.isAvailable();
            Double rating = existingTechnicianProfile != null ? existingTechnicianProfile.getRating() : null;

            savedTechnicianProfile = new TechnicianProfileEntity(employee, specializations, isAvailable, rating);
            savedTechnicianProfile.setId(UUID.randomUUID());
            savedTechnicianProfile = technicianProfileJpaRepository.save(savedTechnicianProfile);
        }

        return new ProfileSnapshot(savedAdminProfile, savedTechnicianProfile);
    }

    private List<String> resolveScopePermissions(
            boolean superAdmin,
            List<String> requestedPermissions,
            AdminProfileEntity existingAdminProfile
    ) {
        List<String> basePermissions;
        if (requestedPermissions != null) {
            basePermissions = normalizeStringList(requestedPermissions);
        } else if (existingAdminProfile != null && existingAdminProfile.getScopePermissions() != null) {
            basePermissions = normalizeStringList(existingAdminProfile.getScopePermissions());
        } else {
            basePermissions = List.of();
        }

        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        for (String permission : basePermissions) {
            if (!"SUPER_ADMIN".equalsIgnoreCase(permission)) {
                permissions.add(permission);
            }
        }

        if (superAdmin) {
            permissions.add("SUPER_ADMIN");
        }

        return List.copyOf(permissions);
    }

    private List<String> resolveSpecializations(
            List<String> requestedSpecializations,
            TechnicianProfileEntity existingTechnicianProfile
    ) {
        if (requestedSpecializations != null) {
            return normalizeStringList(requestedSpecializations);
        }

        if (existingTechnicianProfile != null && existingTechnicianProfile.getSpecializations() != null) {
            return normalizeStringList(existingTechnicianProfile.getSpecializations());
        }

        return List.of();
    }

    private List<String> normalizeStringList(List<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isBlank()) {
                normalized.add(trimmed);
            }
        }
        return List.copyOf(normalized);
    }

    private AdminEmployeeItem mapEmployee(
            UserEntity employee,
            AdminProfileEntity adminProfile,
            TechnicianProfileEntity technicianProfile
    ) {
        String roleCode = resolveRoleCode(employee, adminProfile);
        String roleLabel = resolveRoleLabel(roleCode);
        String department = resolveDepartment(roleCode);
        Instant lastActiveAt = employee.getUpdatedAt() != null ? employee.getUpdatedAt() : employee.getCreatedAt();
        String baseUrl = "/v1/admin/employees/" + employee.getId();

        return new AdminEmployeeItem(
                employee.getId(),
                buildFullName(employee.getFirstName(), employee.getLastName()),
                buildInitials(employee.getFirstName(), employee.getLastName()),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                roleCode,
                roleLabel,
                department,
                employee.isActive() ? AdminEmployeeStatusFilter.ACTIVE.name() : AdminEmployeeStatusFilter.INACTIVE.name(),
                employee.isActive(),
                employee.isPhoneVerified(),
                employee.getAuthProvider(),
                employee.getHireDate(),
                employee.getDepartment(),
                lastActiveAt,
                adminProfile != null && adminProfile.getScopePermissions() != null
                        ? List.copyOf(adminProfile.getScopePermissions())
                        : List.of(),
                technicianProfile != null && technicianProfile.getSpecializations() != null
                        ? List.copyOf(technicianProfile.getSpecializations())
                        : List.of(),
                technicianProfile != null ? technicianProfile.isAvailable() : null,
                baseUrl,
                baseUrl,
                baseUrl + "/permissions",
                baseUrl + "/status",
                baseUrl + "/reset-password",
                baseUrl
        );
    }

    private List<Role> resolveDbRoles(AdminEmployeeRoleFilter roleFilter) {
        if (roleFilter == null || roleFilter == AdminEmployeeRoleFilter.ALL) {
            return STAFF_ROLES;
        }

        return switch (roleFilter) {
            case SUPER_ADMIN, ADMIN -> List.of(Role.ADMIN);
            case TECHNICIAN -> List.of(Role.TECHNICIAN);
            case SECURITY_STAFF -> List.of(Role.SECURITY_GUARD);
            case ALL -> STAFF_ROLES;
        };
    }

    private String resolveStatusFilter(AdminEmployeeStatusFilter status) {
        if (status == null || status == AdminEmployeeStatusFilter.ALL) {
            return null;
        }
        return status.name();
    }

    private boolean matchesRoleFilter(String roleCode, AdminEmployeeRoleFilter roleFilter) {
        if (roleFilter == null || roleFilter == AdminEmployeeRoleFilter.ALL) {
            return true;
        }
        return roleFilter.name().equals(roleCode);
    }

    private String resolveRoleCode(UserEntity employee, AdminProfileEntity adminProfile) {
        if (employee.getRole() == Role.ADMIN) {
            return isSuperAdmin(adminProfile)
                    ? AdminEmployeeRoleFilter.SUPER_ADMIN.name()
                    : AdminEmployeeRoleFilter.ADMIN.name();
        }
        if (employee.getRole() == Role.TECHNICIAN) {
            return AdminEmployeeRoleFilter.TECHNICIAN.name();
        }
        if (employee.getRole() == Role.SECURITY_GUARD) {
            return AdminEmployeeRoleFilter.SECURITY_STAFF.name();
        }
        return employee.getRole().name();
    }

    private String resolveRoleLabel(String roleCode) {
        return switch (roleCode) {
            case "SUPER_ADMIN" -> "Super Admin";
            case "ADMIN" -> "Admin";
            case "TECHNICIAN" -> "Technician";
            case "SECURITY_STAFF" -> "Security Staff";
            default -> roleCode;
        };
    }

    private String resolveDepartment(String roleCode) {
        return switch (roleCode) {
            case "SUPER_ADMIN" -> "-";
            case "ADMIN" -> "Management";
            case "TECHNICIAN" -> "Maintenance";
            case "SECURITY_STAFF" -> "Security";
            default -> "-";
        };
    }

    private boolean isSuperAdmin(AdminProfileEntity adminProfile) {
        if (adminProfile == null || adminProfile.getScopePermissions() == null) {
            return false;
        }
        return adminProfile.getScopePermissions().stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .anyMatch(permission -> "SUPER_ADMIN".equalsIgnoreCase(permission.trim()));
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BusinessRuleException("phoneNumber is required");
        }
        return phoneNumber.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim().toLowerCase(Locale.ROOT);
        return trimmed.isBlank() ? null : trimmed;
    }

    private String buildFullName(String firstName, String lastName) {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();
        String fullName = (f + " " + l).trim();
        return fullName.isBlank() ? "Unknown Employee" : fullName;
    }

    private String buildInitials(String firstName, String lastName) {
        String f = firstName == null ? "" : firstName.trim();
        String l = lastName == null ? "" : lastName.trim();

        String firstInitial = f.isBlank() ? "" : f.substring(0, 1).toUpperCase(Locale.ROOT);
        String lastInitial = l.isBlank() ? "" : l.substring(0, 1).toUpperCase(Locale.ROOT);
        String initials = (firstInitial + lastInitial).trim();

        return initials.isBlank() ? "--" : initials;
    }

    private void ensureUniquePhone(String phoneNumber, UUID employeeIdToExclude) {
        userJpaRepository.findByPhone(phoneNumber).ifPresent(existing -> {
            if (employeeIdToExclude == null || !existing.getId().equals(employeeIdToExclude)) {
                throw new BusinessRuleException("Phone number already registered");
            }
        });
    }

    private void ensureUniqueEmail(String email, UUID employeeIdToExclude) {
        if (email == null) {
            return;
        }

        userJpaRepository.findByEmail(email).ifPresent(existing -> {
            if (employeeIdToExclude == null || !existing.getId().equals(employeeIdToExclude)) {
                throw new BusinessRuleException("Email already registered");
            }
        });
    }

    private void validateCreateRequest(CreateEmployeeRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }
        resolveNameParts(request.fullName(), request.firstName(), request.lastName());
        if (request.role() == null || request.role().isBlank()) {
            throw new BusinessRuleException("role is required");
        }
        if (request.phoneNumber() == null || request.phoneNumber().isBlank()) {
            throw new BusinessRuleException("phoneNumber is required");
        }
        if (request.loginMethod() == LoginMethod.EMAIL_PASSWORD && isBlank(request.email())) {
            throw new BusinessRuleException("email is required when loginMethod is EMAIL_PASSWORD");
        }
        if (request.hireDate() == null) {
            throw new BusinessRuleException("hireDate is required");
        }
        if (request.hireDate().isAfter(LocalDate.now().plusDays(1))) {
            throw new BusinessRuleException("hireDate cannot be in the future");
        }
        if (request.password() != null && request.confirmPassword() != null && !request.password().equals(request.confirmPassword())) {
            throw new BusinessRuleException("password and confirmPassword do not match");
        }
    }

    private NameParts resolveNameParts(String fullName, String firstName, String lastName) {
        if (!isBlank(fullName)) {
            String normalized = fullName.trim().replaceAll("\\s+", " ");
            int firstSpace = normalized.indexOf(' ');
            if (firstSpace <= 0 || firstSpace == normalized.length() - 1) {
                throw new BusinessRuleException("fullName must include first and last name");
            }
            String resolvedFirstName = normalized.substring(0, firstSpace).trim();
            String resolvedLastName = normalized.substring(firstSpace + 1).trim();
            return new NameParts(resolvedFirstName, resolvedLastName);
        }

        if (isBlank(firstName) || isBlank(lastName)) {
            throw new BusinessRuleException("fullName is required, or both firstName and lastName");
        }

        return new NameParts(firstName.trim(), lastName.trim());
    }

    private String resolveDepartmentValue(String explicitDepartment) {
        if (explicitDepartment != null && !explicitDepartment.isBlank()) {
            return explicitDepartment.trim();
        }
        return null;
    }

    private RoleSelection resolveRoleForCreate(String role, Boolean isSuperAdmin) {
        return parseRequestedRole(role, isSuperAdmin, true);
    }

    private RoleSelection resolveRoleForUpdate(
            Role currentRole,
            AdminProfileEntity currentAdminProfile,
            String requestedRole,
            Boolean isSuperAdmin
    ) {
        if (isBlank(requestedRole)) {
            if (currentRole != Role.ADMIN) {
                return new RoleSelection(currentRole, false);
            }

            boolean currentIsSuperAdmin = isSuperAdmin(currentAdminProfile);
            boolean resolvedSuperAdmin = isSuperAdmin == null ? currentIsSuperAdmin : isSuperAdmin;
            return new RoleSelection(Role.ADMIN, resolvedSuperAdmin);
        }

        return parseRequestedRole(requestedRole, isSuperAdmin, false);
    }

    private RoleSelection parseRequestedRole(String role, Boolean isSuperAdmin, boolean required) {
        if (isBlank(role)) {
            if (required) {
                throw new BusinessRuleException("role is required");
            }
            return new RoleSelection(Role.ADMIN, false);
        }

        String normalizedRole = role.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalizedRole) {
            case "SUPER_ADMIN" -> new RoleSelection(Role.ADMIN, true);
            case "ADMIN" -> new RoleSelection(Role.ADMIN, Boolean.TRUE.equals(isSuperAdmin));
            case "TECHNICIAN" -> new RoleSelection(Role.TECHNICIAN, false);
            case "SECURITY_STAFF", "SECURITY_GUARD" -> new RoleSelection(Role.SECURITY_GUARD, false);
            default -> throw new BusinessRuleException("Unsupported role: " + role);
        };
    }

    private String generateTempPassword(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(TEMP_PASSWORD_CHARS.length());
            builder.append(TEMP_PASSWORD_CHARS.charAt(index));
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record RoleSelection(
            Role databaseRole,
            boolean superAdmin
    ) {
    }

    private record ProfileSnapshot(
            AdminProfileEntity adminProfile,
            TechnicianProfileEntity technicianProfile
    ) {
    }

    private record NameParts(
            String firstName,
            String lastName
    ) {
    }

    public record AdminEmployeesDashboardResponse(
            List<AdminEmployeeItem> employees,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious,
            AdminEmployeesSummary summary,
            List<String> roleOptions,
            List<String> statusOptions
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

    public record AdminEmployeeItem(
            UUID employeeId,
            String fullName,
            String initials,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String roleCode,
            String roleLabel,
            String department,
            String status,
            boolean isActive,
            boolean isPhoneVerified,
            LoginMethod loginMethod,
            LocalDate hireDate,
            String customDepartment,
            Instant lastActiveAt,
            List<String> scopePermissions,
            List<String> specializations,
            Boolean technicianAvailable,
            String viewUrl,
            String editUrl,
            String permissionsUrl,
            String statusUrl,
            String resetPasswordUrl,
            String deactivateUrl
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

    public record UpdateEmployeeStatusRequest(Boolean isActive) {
    }

    public record CreateEmployeeResult(
            UUID employeeId,
            String message,
            String temporaryPassword,
            AdminEmployeeItem employee
    ) {
    }

    public record ResetPasswordResult(
            UUID employeeId,
            String message,
            String temporaryPassword
    ) {
    }
}
