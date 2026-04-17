package com.theMs.sakany.community.internal.api.controllers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.theMs.sakany.community.internal.application.queries.AdminFeedbackDashboardService;
import com.theMs.sakany.community.internal.application.queries.AdminFeedbackStatusFilter;
import com.theMs.sakany.community.internal.application.queries.AdminFeedbackTab;
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
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/feedback")
public class AdminFeedbackController {

    private final AdminFeedbackDashboardService adminFeedbackDashboardService;

    public AdminFeedbackController(AdminFeedbackDashboardService adminFeedbackDashboardService) {
        this.adminFeedbackDashboardService = adminFeedbackDashboardService;
    }

    @GetMapping
    public ResponseEntity<AdminFeedbackDashboardService.AdminFeedbackDashboardResponse> getDashboard(
            @RequestParam(required = false, defaultValue = "PUBLIC_SUGGESTIONS") AdminFeedbackTab tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALL") AdminFeedbackStatusFilter status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(adminFeedbackDashboardService.getDashboard(tab, search, status, category, page, size));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategoryOptions() {
        return ResponseEntity.ok(adminFeedbackDashboardService.getCategoryOptions());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatusOptions() {
        return ResponseEntity.ok(adminFeedbackDashboardService.getStatusOptions());
    }

    @GetMapping("/{feedbackId}/details")
    public ResponseEntity<AdminFeedbackDashboardService.AdminFeedbackDetailsResponse> getFeedbackDetails(@PathVariable UUID feedbackId) {
        return ResponseEntity.ok(adminFeedbackDashboardService.getFeedbackDetails(feedbackId));
    }

    @PostMapping
    public ResponseEntity<UUID> createFeedback(@RequestBody AdminCreateFeedbackRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        UUID authorId = request.authorId() != null ? request.authorId() : getAuthenticatedUserId();
        Boolean isPublic = request.resolveIsPublic();

        UUID feedbackId = adminFeedbackDashboardService.createFeedback(
                authorId,
                request.title(),
                request.content(),
                request.type(),
                isPublic,
                request.category(),
                request.location(),
                request.isAnonymous(),
                request.resolveImageUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackId);
    }

    @PatchMapping("/{feedbackId}/respond")
    public ResponseEntity<Void> respondToFeedback(
            @PathVariable UUID feedbackId,
            @RequestBody AdminRespondFeedbackRequest request
    ) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        adminFeedbackDashboardService.respondToFeedback(feedbackId, request.response(), request.newStatus());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{feedbackId}/status")
    public ResponseEntity<Void> updateFeedbackStatus(
            @PathVariable UUID feedbackId,
            @RequestBody AdminUpdateFeedbackStatusRequest request
    ) {
        if (request == null) {
            throw new BusinessRuleException("Request body is required");
        }

        adminFeedbackDashboardService.updateFeedbackStatus(feedbackId, request.newStatus());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable UUID feedbackId) {
        adminFeedbackDashboardService.deleteFeedback(feedbackId);
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
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid authenticated principal");
        }
    }

    public record AdminCreateFeedbackRequest(
            @JsonAlias({"residentId", "selectedResidentId", "submitOnBehalfOf", "onBehalfOfResidentId", "userId"})
            UUID authorId,
            @JsonAlias({"subject", "briefDescription"})
            String title,
            @JsonAlias({"description", "body", "message", "detailedDescription", "details"})
            String content,
            FeedbackType type,
            @JsonAlias({"public", "isSuggestionPublic"})
            Boolean isPublic,
            @JsonAlias({"private"})
            Boolean isPrivate,
            @JsonAlias({"visibility", "audience", "privacy", "postVisibility"})
            String visibility,
            @JsonAlias({"selectedCategory", "feedbackCategory"})
            String category,
            String location,
            @JsonAlias({"postAnonymously", "anonymous"})
            Boolean isAnonymous,
            @JsonAlias({"photoUrl", "image", "coverImageUrl"})
            String imageUrl,
            @JsonAlias({"photoUrls", "images", "attachments", "photos"})
            List<String> imageUrls
    ) {
        public Boolean resolveIsPublic() {
            if (isPublic != null) {
                return isPublic;
            }
            if (isPrivate != null) {
                return !isPrivate;
            }
            if (visibility != null && !visibility.isBlank()) {
                String normalized = visibility.trim().toUpperCase(Locale.ROOT);
                if (normalized.equals("PUBLIC")) {
                    return true;
                }
                if (normalized.equals("PRIVATE")) {
                    return false;
                }
            }
            return null;
        }

        public String resolveImageUrl() {
            if (imageUrl != null && !imageUrl.isBlank()) {
                return imageUrl.trim();
            }

            if (imageUrls == null || imageUrls.isEmpty()) {
                return null;
            }

            return imageUrls.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .findFirst()
                    .orElse(null);
        }
    }

    public record AdminRespondFeedbackRequest(
            @JsonAlias({"adminResponse", "reply", "message"})
            String response,
            AdminFeedbackStatusFilter newStatus
    ) {
    }

    public record AdminUpdateFeedbackStatusRequest(AdminFeedbackStatusFilter newStatus) {
    }
}
