package com.theMs.sakany.access.internal.api.controllers;

import com.theMs.sakany.access.internal.api.dtos.AccessCodeResponse;
import com.theMs.sakany.access.internal.api.dtos.CreateAccessCodeRequest;
import com.theMs.sakany.access.internal.api.dtos.ReactivateAccessCodeRequest;
import com.theMs.sakany.access.internal.api.dtos.ScanAccessCodeRequest;
import com.theMs.sakany.access.internal.api.dtos.ScanAccessCodeResponse;
import com.theMs.sakany.access.internal.api.dtos.VisitLogResponse;
import com.theMs.sakany.access.internal.application.commands.CreateAccessCodeCommand;
import com.theMs.sakany.access.internal.application.commands.CreateAccessCodeCommandHandler;
import com.theMs.sakany.access.internal.application.commands.LogVisitorExitCommand;
import com.theMs.sakany.access.internal.application.commands.LogVisitorExitCommandHandler;
import com.theMs.sakany.access.internal.application.commands.ReactivateAccessCodeCommand;
import com.theMs.sakany.access.internal.application.commands.ReactivateAccessCodeCommandHandler;
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
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ReactivateAccessCodeCommandHandler reactivateAccessCodeHandler;

    public AccessController(
        CreateAccessCodeCommandHandler createAccessCodeHandler,
        ScanAccessCodeCommandHandler scanAccessCodeHandler,
        RevokeAccessCodeCommandHandler revokeAccessCodeHandler,
        LogVisitorExitCommandHandler logVisitorExitHandler,
        GetAccessCodeQueryHandler getAccessCodeHandler,
        ListAccessCodesQueryHandler listAccessCodesHandler,
        ListVisitLogsQueryHandler listVisitLogsHandler,
        ReactivateAccessCodeCommandHandler reactivateAccessCodeHandler
    ) {
        this.createAccessCodeHandler = createAccessCodeHandler;
        this.scanAccessCodeHandler = scanAccessCodeHandler;
        this.revokeAccessCodeHandler = revokeAccessCodeHandler;
        this.logVisitorExitHandler = logVisitorExitHandler;
        this.getAccessCodeHandler = getAccessCodeHandler;
        this.listAccessCodesHandler = listAccessCodesHandler;
        this.listVisitLogsHandler = listVisitLogsHandler;
        this.reactivateAccessCodeHandler = reactivateAccessCodeHandler;
    }

    /**
     * POST /v1/access/codes — Create a new access code (by resident)
     */
    @PostMapping("/codes")
    public ResponseEntity<AccessCodeResponse> createAccessCode(
        @RequestBody CreateAccessCodeRequest request
    ) {
        UUID residentId = getAuthenticatedUserId();

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
        AccessCode accessCode = getAccessCodeHandler.handle(new GetAccessCodeQuery(accessCodeId));
        return new ResponseEntity<>(toAccessCodeResponse(accessCode), HttpStatus.CREATED);
    }

    /**
     * GET /v1/access/codes/{id} — Get access code details
     */
    @GetMapping("/codes/{id}")
    public ResponseEntity<AccessCodeResponse> getAccessCode(@PathVariable UUID id) {
        GetAccessCodeQuery query = new GetAccessCodeQuery(id);
        AccessCode accessCode = getAccessCodeHandler.handle(query);

        return ResponseEntity.ok(toAccessCodeResponse(accessCode));
    }

    /**
     * GET /v1/access/codes/my — List resident's access codes
     */
    @GetMapping("/codes/my")
    public ResponseEntity<List<AccessCodeResponse>> listMyAccessCodes() {
        UUID residentId = getAuthenticatedUserId();

        ListAccessCodesQuery query = new ListAccessCodesQuery(residentId);
        List<AccessCode> accessCodes = listAccessCodesHandler.handle(query);

        List<AccessCodeResponse> responses = accessCodes.stream()
            .map(this::toAccessCodeResponse)
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
     * POST /v1/access/codes/{codeId}/reactivate — Reactivate an expired/used access code
     */
    @PostMapping("/codes/{codeId}/reactivate")
        public ResponseEntity<AccessCodeResponse> reactivateAccessCode(
            @PathVariable UUID codeId,
            @RequestBody ReactivateAccessCodeRequest request
    ) {
        UUID residentId = getAuthenticatedUserId();
        ReactivateAccessCodeCommand command = new ReactivateAccessCodeCommand(
            residentId,
            codeId,
            request.validFrom(),
            request.validUntil()
        );
        UUID newCodeId = reactivateAccessCodeHandler.handle(command);
        AccessCode accessCode = getAccessCodeHandler.handle(new GetAccessCodeQuery(newCodeId));
        return ResponseEntity.status(HttpStatus.CREATED).body(toAccessCodeResponse(accessCode));
    }

    /**
     * GET /v1/access/visit-logs — List visit logs for the authenticated resident
     */
    @GetMapping("/visit-logs")
    public ResponseEntity<List<VisitLogResponse>> listVisitLogs() {
        UUID residentId = getAuthenticatedUserId();

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

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }

        try {
            return UUID.fromString(principal.toString());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid authenticated principal");
        }
    }

    private AccessCodeResponse toAccessCodeResponse(AccessCode accessCode) {
        return new AccessCodeResponse(
            accessCode.getId(),
            accessCode.getResidentId(),
            accessCode.getVisitorName(),
            accessCode.getVisitorPhone(),
            accessCode.getPurpose(),
            accessCode.getCode(),
            accessCode.getQrData(),
            accessCode.isSingleUse(),
            accessCode.getValidFrom(),
            accessCode.getValidUntil(),
            accessCode.getStatus(),
            accessCode.getUsedAt()
        );
    }
}
