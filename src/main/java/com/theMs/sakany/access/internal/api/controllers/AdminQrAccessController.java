package com.theMs.sakany.access.internal.api.controllers;

import com.theMs.sakany.access.internal.application.queries.AdminQrAccessDirectoryService;
import com.theMs.sakany.access.internal.application.queries.AdminQrAccessStatus;
import com.theMs.sakany.access.internal.application.queries.AdminQrAccessTab;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/access")
public class AdminQrAccessController {

    private final AdminQrAccessDirectoryService adminQrAccessDirectoryService;

    public AdminQrAccessController(AdminQrAccessDirectoryService adminQrAccessDirectoryService) {
        this.adminQrAccessDirectoryService = adminQrAccessDirectoryService;
    }

    @GetMapping("/codes")
    public ResponseEntity<AdminQrAccessDirectoryService.AdminQrAccessPage> listAdminQrAccessCodes(
            @RequestParam(required = false, defaultValue = "ALL") AdminQrAccessTab tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AdminQrAccessStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(adminQrAccessDirectoryService.listCodes(tab, search, status, page, size));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> listStatusOptions() {
        return ResponseEntity.ok(Arrays.stream(AdminQrAccessStatus.values()).map(Enum::name).toList());
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> listTypeOptions() {
        return ResponseEntity.ok(Arrays.stream(AdminQrAccessTab.values()).map(Enum::name).toList());
    }

    @GetMapping("/residents/{residentId}/codes")
    public ResponseEntity<AdminQrAccessDirectoryService.ResidentQrCodesResponse> listResidentQrCodes(
            @PathVariable UUID residentId,
            @RequestParam(required = false, defaultValue = "ALL") AdminQrAccessTab tab,
            @RequestParam(required = false) AdminQrAccessStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(adminQrAccessDirectoryService.listResidentCodes(residentId, tab, status, page, size));
    }

    @GetMapping("/codes/{accessCodeId}/details")
    public ResponseEntity<AdminQrAccessDirectoryService.AdminQrCodeDetailsResponse> getCodeDetails(
            @PathVariable UUID accessCodeId
    ) {
        return ResponseEntity.ok(adminQrAccessDirectoryService.getCodeDetails(accessCodeId));
    }

    @GetMapping("/codes/{accessCodeId}/download")
    public ResponseEntity<byte[]> downloadQrCode(@PathVariable UUID accessCodeId) {
        byte[] body = adminQrAccessDirectoryService.downloadCode(accessCodeId);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr-code-" + accessCodeId + ".txt")
                .body(body);
    }

    @DeleteMapping("/codes/{accessCodeId}")
    public ResponseEntity<Void> deleteQrCode(@PathVariable UUID accessCodeId) {
        adminQrAccessDirectoryService.deleteCode(accessCodeId);
        return ResponseEntity.noContent().build();
    }
}
