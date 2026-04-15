package com.theMs.sakany.access.internal.api.controllers;

import com.theMs.sakany.access.internal.api.dtos.AccessCodeResponse;
import com.theMs.sakany.access.internal.api.dtos.CreateAccessCodeRequest;
import com.theMs.sakany.access.internal.api.dtos.ScanAccessCodeRequest;
import com.theMs.sakany.access.internal.api.dtos.ScanAccessCodeResponse;
import com.theMs.sakany.access.internal.api.dtos.VisitLogResponse;
import com.theMs.sakany.access.internal.application.commands.CreateAccessCodeCommand;
import com.theMs.sakany.access.internal.application.commands.CreateAccessCodeCommandHandler;
import com.theMs.sakany.access.internal.application.commands.LogVisitorExitCommand;
import com.theMs.sakany.access.internal.application.commands.LogVisitorExitCommandHandler;
import com.theMs.sakany.access.internal.application.commands.RevokeAccessCodeCommand;
import com.theMs.sakany.access.internal.application.commands.RevokeAccessCodeCommandHandler;
import com.theMs.sakany.access.internal.application.commands.ScanAccessCodeCommand;
import com.theMs.sakany.access.internal.application.commands.ScanAccessCodeCommandHandler;
import com.theMs.sakany.access.internal.application.queries.GetAccessCodeQuery;
import com.theMs.sakany.access.internal.application.queries.GetAccessCodeQueryHandler;
import com.theMs.sakany.access.internal.application.queries.ListAccessCodesQuery;
import com.theMs.sakany.access.internal.application.queries.ListAccessCodesQueryHandler;
import com.theMs.sakany.access.internal.application.queries.ListVisitLogsQuery;
import com.theMs.sakany.access.internal.application.queries.ListVisitLogsQueryHandler;
import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.VisitLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/access")
public class AccessController {

    private final CreateAccessCodeCommandHandler createAccessCodeHandler;
    private final ScanAccessCodeCommandHandler scanAccessCodeHandler;
    private final RevokeAccessCodeCommandHandler revokeAccessCodeHandler;
    private final LogVisitorExitCommandHandler logVisitorExitHandler;
    private final GetAccessCodeQueryHandler getAccessCodeHandler;
    private final ListAccessCodesQueryHandler listAccessCodesHandler;
    private final ListVisitLogsQueryHandler listVisitLogsHandler;

    public AccessController(
        CreateAccessCodeCommandHandler createAccessCodeHandler,
        ScanAccessCodeCommandHandler scanAccessCodeHandler,
        RevokeAccessCodeCommandHandler revokeAccessCodeHandler,
        LogVisitorExitCommandHandler logVisitorExitHandler,
        GetAccessCodeQueryHandler getAccessCodeHandler,
        ListAccessCodesQueryHandler listAccessCodesHandler,
        ListVisitLogsQueryHandler listVisitLogsHandler
    ) {
        this.createAccessCodeHandler = createAccessCodeHandler;
        this.scanAccessCodeHandler = scanAccessCodeHandler;
        this.revokeAccessCodeHandler = revokeAccessCodeHandler;
        this.logVisitorExitHandler = logVisitorExitHandler;
        this.getAccessCodeHandler = getAccessCodeHandler;
        this.listAccessCodesHandler = listAccessCodesHandler;
        this.listVisitLogsHandler = listVisitLogsHandler;
    }

    /**
     * POST /v1/access/codes — Create a new access code (by resident)
     */
    @PostMapping("/codes")
    public ResponseEntity<UUID> createAccessCode(
        @RequestBody CreateAccessCodeRequest request
    ) {
        // TODO: Extract residentId from authenticated user context
        UUID residentId = UUID.randomUUID(); // Placeholder - will come from authentication

        CreateAccessCodeCommand command = new CreateAccessCodeCommand(
            residentId,
            request.visitorName(),
            request.visitorPhone(),
            request.purpose(),
            request.isSingleUse(),
            request.validFrom(),
            request.validUntil()
        );

        UUID accessCodeId = createAccessCodeHandler.handle(command);
        return new ResponseEntity<>(accessCodeId, HttpStatus.CREATED);
    }

    /**
     * GET /v1/access/codes/{id} — Get access code details
     */
    @GetMapping("/codes/{id}")
    public ResponseEntity<AccessCodeResponse> getAccessCode(@PathVariable UUID id) {
        GetAccessCodeQuery query = new GetAccessCodeQuery(id);
        AccessCode accessCode = getAccessCodeHandler.handle(query);
        
        AccessCodeResponse response = new AccessCodeResponse(
            accessCode.getId(),
            accessCode.getResidentId(),
            accessCode.getVisitorName(),
            accessCode.getVisitorPhone(),
            accessCode.getPurpose(),
            accessCode.getCode(),
            accessCode.isSingleUse(),
            accessCode.getValidFrom(),
            accessCode.getValidUntil(),
            accessCode.getStatus(),
            accessCode.getUsedAt()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /v1/access/codes/my — List resident's access codes
     */
    @GetMapping("/codes/my")
    public ResponseEntity<List<AccessCodeResponse>> listMyAccessCodes() {
        // TODO: Extract residentId from authenticated user context
        UUID residentId = UUID.randomUUID(); // Placeholder - will come from authentication

        ListAccessCodesQuery query = new ListAccessCodesQuery(residentId);
        List<AccessCode> accessCodes = listAccessCodesHandler.handle(query);

        List<AccessCodeResponse> responses = accessCodes.stream()
            .map(ac -> new AccessCodeResponse(
                ac.getId(),
                ac.getResidentId(),
                ac.getVisitorName(),
                ac.getVisitorPhone(),
                ac.getPurpose(),
                ac.getCode(),
                ac.isSingleUse(),
                ac.getValidFrom(),
                ac.getValidUntil(),
                ac.getStatus(),
                ac.getUsedAt()
            ))
            .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * POST /v1/access/codes/{code}/scan — Scan/use an access code (by security guard)
     */
    @PostMapping("/codes/{code}/scan")
    public ResponseEntity<ScanAccessCodeResponse> scanAccessCode(
        @PathVariable String code,
        @RequestBody ScanAccessCodeRequest request
    ) {
        ScanAccessCodeCommand command = new ScanAccessCodeCommand(code, request.gateNumber());
        UUID visitLogId = scanAccessCodeHandler.handle(command);
        
        return new ResponseEntity<>(
            new ScanAccessCodeResponse(visitLogId),
            HttpStatus.CREATED
        );
    }

    /**
     * DELETE /v1/access/codes/{id} — Revoke an access code
     */
    @DeleteMapping("/codes/{id}")
    public ResponseEntity<Void> revokeAccessCode(@PathVariable UUID id) {
        RevokeAccessCodeCommand command = new RevokeAccessCodeCommand(id);
        revokeAccessCodeHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /v1/access/visit-logs — List visit logs (admin)
     */
    @GetMapping("/visit-logs")
    public ResponseEntity<List<VisitLogResponse>> listVisitLogs() {
        // TODO: Extract residentId from authenticated user context (or list all for admin)
        UUID residentId = UUID.randomUUID(); // Placeholder - will come from authentication

        ListVisitLogsQuery query = new ListVisitLogsQuery(residentId);
        List<VisitLog> visitLogs = listVisitLogsHandler.handle(query);

        List<VisitLogResponse> responses = visitLogs.stream()
            .map(vl -> new VisitLogResponse(
                vl.getId(),
                vl.getAccessCodeId(),
                vl.getResidentId(),
                vl.getVisitorName(),
                vl.getEntryTime(),
                vl.getExitTime(),
                vl.getGateNumber()
            ))
            .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * PATCH /v1/access/visit-logs/{id}/exit — Log visitor exit
     */
    @PatchMapping("/visit-logs/{id}/exit")
    public ResponseEntity<Void> logVisitorExit(@PathVariable UUID id) {
        LogVisitorExitCommand command = new LogVisitorExitCommand(id);
        logVisitorExitHandler.handle(command);
        return ResponseEntity.noContent().build();
    }
}
