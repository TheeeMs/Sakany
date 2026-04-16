package com.theMs.sakany.community.internal.api.controllers;

import com.theMs.sakany.community.internal.application.commands.SubmitFeedbackCommand;
import com.theMs.sakany.community.internal.application.queries.AdminResidentFeedbackService;
import com.theMs.sakany.community.internal.domain.FeedbackType;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/feedback")
public class AdminResidentFeedbackController {

    private final AdminResidentFeedbackService adminResidentFeedbackService;
    private final com.theMs.sakany.shared.cqrs.CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler;

    public AdminResidentFeedbackController(
            AdminResidentFeedbackService adminResidentFeedbackService,
            com.theMs.sakany.shared.cqrs.CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler
    ) {
        this.adminResidentFeedbackService = adminResidentFeedbackService;
        this.submitFeedbackHandler = submitFeedbackHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> createFeedback(@RequestBody AdminCreateFeedbackRequest request) {
        UUID authorId = request.authorId() != null ? request.authorId() : getAuthenticatedUserId();

        UUID feedbackId = submitFeedbackHandler.handle(new SubmitFeedbackCommand(
                authorId,
                request.title(),
                request.content(),
                request.type(),
                request.isPublic(),
                request.category(),
                request.location(),
                request.isAnonymous(),
                request.imageUrl()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackId);
    }

    @GetMapping
    public ResponseEntity<AdminResidentFeedbackService.AdminResidentFeedbackResponse> getDashboard(
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(adminResidentFeedbackService.getDashboard(tab, status, category, search));
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<AdminResidentFeedbackService.FeedbackCardItem> getDetails(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok(adminResidentFeedbackService.getDetails(feedbackId));
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatusOptions() {
        return ResponseEntity.ok(adminResidentFeedbackService.getStatusOptions());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoryOptions(@RequestParam(required = false) String tab) {
        return ResponseEntity.ok(adminResidentFeedbackService.getCategoryOptions(tab));
    }

    @PatchMapping("/{feedbackId}/respond")
    public ResponseEntity<Void> respond(
            @PathVariable UUID feedbackId,
            @RequestBody AdminFeedbackRespondRequest request
    ) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }
        adminResidentFeedbackService.respond(feedbackId, request.response(), request.newStatus());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> delete(@PathVariable UUID feedbackId) {
        adminResidentFeedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }

    public record AdminFeedbackRespondRequest(
            String response,
            String newStatus
    ) {
    }

    public record AdminCreateFeedbackRequest(
            UUID authorId,
            String title,
            String content,
            FeedbackType type,
            boolean isPublic,
            String category,
            String location,
            boolean isAnonymous,
            String imageUrl
    ) {
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
}
