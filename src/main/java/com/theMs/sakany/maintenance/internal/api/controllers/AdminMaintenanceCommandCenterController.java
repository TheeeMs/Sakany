package com.theMs.sakany.maintenance.internal.api.controllers;

import com.theMs.sakany.maintenance.internal.application.queries.AdminMaintenanceCommandCenterService;
import com.theMs.sakany.maintenance.internal.application.queries.AdminMaintenanceRequestType;
import com.theMs.sakany.maintenance.internal.application.queries.AdminMaintenanceSortBy;
import com.theMs.sakany.maintenance.internal.application.queries.AdminMaintenanceTab;
import com.theMs.sakany.maintenance.internal.domain.MaintenanceCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/maintenance")
public class AdminMaintenanceCommandCenterController {

    private final AdminMaintenanceCommandCenterService commandCenterService;

    public AdminMaintenanceCommandCenterController(AdminMaintenanceCommandCenterService commandCenterService) {
        this.commandCenterService = commandCenterService;
    }

    @GetMapping("/requests")
    public ResponseEntity<AdminMaintenanceCommandCenterService.MaintenanceCommandCenterPage> getCommandCenter(
            @RequestParam(required = false, defaultValue = "ALL") AdminMaintenanceTab tab,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) AdminMaintenanceRequestType type,
            @RequestParam(required = false) MaintenanceCategory category,
            @RequestParam(required = false, defaultValue = "NEWEST") AdminMaintenanceSortBy sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(commandCenterService.getCommandCenter(tab, area, type, category, sortBy, page, size));
    }

    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAreaOptions() {
        return ResponseEntity.ok(commandCenterService.getAreaOptions());
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getTypeOptions() {
        return ResponseEntity.ok(Arrays.stream(AdminMaintenanceRequestType.values()).map(Enum::name).toList());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoryOptions() {
        return ResponseEntity.ok(Arrays.stream(MaintenanceCategory.values()).map(Enum::name).toList());
    }
}
