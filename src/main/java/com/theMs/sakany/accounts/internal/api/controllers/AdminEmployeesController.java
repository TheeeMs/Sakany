package com.theMs.sakany.accounts.internal.api.controllers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.theMs.sakany.accounts.internal.application.queries.AdminEmployeeDirectoryService;
import com.theMs.sakany.accounts.internal.application.queries.AdminEmployeeRoleFilter;
import com.theMs.sakany.accounts.internal.application.queries.AdminEmployeeStatusFilter;
import com.theMs.sakany.accounts.internal.domain.LoginMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/employees")
public class AdminEmployeesController {

    private final AdminEmployeeDirectoryService adminEmployeeDirectoryService;

    public AdminEmployeesController(AdminEmployeeDirectoryService adminEmployeeDirectoryService) {
        this.adminEmployeeDirectoryService = adminEmployeeDirectoryService;
    }

    @GetMapping
    public ResponseEntity<AdminEmployeeDirectoryService.AdminEmployeesDashboardResponse> listEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") AdminEmployeeRoleFilter role,
            @RequestParam(required = false, defaultValue = "ALL") AdminEmployeeStatusFilter status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(adminEmployeeDirectoryService.getEmployees(search, role, status, page, size));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<AdminEmployeeDirectoryService.AdminEmployeeItem> getEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(adminEmployeeDirectoryService.getEmployee(employeeId));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoleOptions() {
        return ResponseEntity.ok(adminEmployeeDirectoryService.getRoleOptions());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatusOptions() {
        return ResponseEntity.ok(adminEmployeeDirectoryService.getStatusOptions());
    }

    @PostMapping
    public ResponseEntity<AdminEmployeeDirectoryService.CreateEmployeeResult> createEmployee(
            @RequestBody AdminCreateEmployeeRequest request
    ) {
        AdminEmployeeDirectoryService.CreateEmployeeResult result = adminEmployeeDirectoryService.createEmployee(
                new AdminEmployeeDirectoryService.CreateEmployeeRequest(
                        request.fullName(),
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.email(),
                        request.password(),
                        request.confirmPassword(),
                        request.role(),
                        request.hireDate(),
                        request.department(),
                        request.isSuperAdmin(),
                        request.isActive(),
                        request.isPhoneVerified(),
                        request.loginMethod(),
                        request.scopePermissions(),
                        request.specializations(),
                        request.technicianAvailable()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{employeeId}")
    public ResponseEntity<Void> updateEmployee(
            @PathVariable UUID employeeId,
            @RequestBody AdminUpdateEmployeeRequest request
    ) {
        adminEmployeeDirectoryService.updateEmployee(
                employeeId,
                new AdminEmployeeDirectoryService.UpdateEmployeeRequest(
                        request.fullName(),
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.email(),
                        request.password(),
                        request.confirmPassword(),
                        request.role(),
                        request.hireDate(),
                        request.department(),
                        request.isSuperAdmin(),
                        request.isActive(),
                        request.isPhoneVerified(),
                        request.loginMethod(),
                        request.scopePermissions(),
                        request.specializations(),
                        request.technicianAvailable()
                )
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{employeeId}/status")
    public ResponseEntity<Void> updateEmployeeStatus(
            @PathVariable UUID employeeId,
            @RequestBody AdminUpdateEmployeeStatusRequest request
    ) {
        adminEmployeeDirectoryService.updateStatus(
                employeeId,
                new AdminEmployeeDirectoryService.UpdateEmployeeStatusRequest(request.isActive())
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{employeeId}/reset-password")
    public ResponseEntity<AdminEmployeeDirectoryService.ResetPasswordResult> resetEmployeePassword(
            @PathVariable UUID employeeId,
            @RequestBody(required = false) AdminResetPasswordRequest request
    ) {
        String newPassword = request != null ? request.newPassword() : null;
        return ResponseEntity.ok(adminEmployeeDirectoryService.resetPassword(employeeId, newPassword));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable UUID employeeId) {
        adminEmployeeDirectoryService.deactivateEmployee(employeeId);
        return ResponseEntity.noContent().build();
    }

    public record AdminCreateEmployeeRequest(
            @JsonAlias({"employeeName", "name"}) String fullName,
            String firstName,
            String lastName,
            @JsonAlias({"phone", "mobileNumber"}) String phoneNumber,
            @JsonAlias({"emailAddress", "userEmail"}) String email,
            String password,
            @JsonAlias({"confirm_password"}) String confirmPassword,
            @JsonAlias({"employeeRole", "roleCode"}) String role,
            @JsonAlias({"joiningDate", "startDate"}) LocalDate hireDate,
            @JsonAlias({"departmentName"}) String department,
            @JsonAlias({"superAdmin"}) Boolean isSuperAdmin,
            @JsonAlias({"active"}) Boolean isActive,
            @JsonAlias({"phoneVerified"}) Boolean isPhoneVerified,
            @JsonAlias({"authProvider"}) LoginMethod loginMethod,
            List<String> scopePermissions,
            List<String> specializations,
            @JsonAlias({"isAvailable"}) Boolean technicianAvailable
    ) {
    }

    public record AdminUpdateEmployeeRequest(
            @JsonAlias({"employeeName", "name"}) String fullName,
            String firstName,
            String lastName,
            @JsonAlias({"phone", "mobileNumber"}) String phoneNumber,
            @JsonAlias({"emailAddress", "userEmail"}) String email,
            String password,
            @JsonAlias({"confirm_password"}) String confirmPassword,
            @JsonAlias({"employeeRole", "roleCode"}) String role,
            @JsonAlias({"joiningDate", "startDate"}) LocalDate hireDate,
            @JsonAlias({"departmentName"}) String department,
            @JsonAlias({"superAdmin"}) Boolean isSuperAdmin,
            @JsonAlias({"active"}) Boolean isActive,
            @JsonAlias({"phoneVerified"}) Boolean isPhoneVerified,
            @JsonAlias({"authProvider"}) LoginMethod loginMethod,
            List<String> scopePermissions,
            List<String> specializations,
            @JsonAlias({"isAvailable"}) Boolean technicianAvailable
    ) {
    }

    public record AdminUpdateEmployeeStatusRequest(Boolean isActive) {
    }

    public record AdminResetPasswordRequest(String newPassword) {
    }
}
