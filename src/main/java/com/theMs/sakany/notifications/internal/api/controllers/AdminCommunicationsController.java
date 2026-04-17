package com.theMs.sakany.notifications.internal.api.controllers;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.theMs.sakany.notifications.internal.application.queries.AdminCommunicationsCenterService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/communications")
public class AdminCommunicationsController {

    private final AdminCommunicationsCenterService adminCommunicationsCenterService;

    public AdminCommunicationsController(AdminCommunicationsCenterService adminCommunicationsCenterService) {
        this.adminCommunicationsCenterService = adminCommunicationsCenterService;
    }

    @GetMapping("/center")
    public ResponseEntity<AdminCommunicationsCenterService.AdminCommunicationsCenterResponse> getCenter(
            @RequestParam(required = false, defaultValue = "PUSH_NOTIFICATIONS") String tab,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(adminCommunicationsCenterService.getCenter(tab, status, search));
    }

    @PostMapping("/notifications")
    public ResponseEntity<AdminCommunicationsCenterService.CreatePushNotificationResult> createPushNotification(
            @RequestBody AdminCreatePushNotificationRequest request
    ) {
        UUID adminId = resolveAuthenticatedAdminActor(request.adminId(), "adminId");

        AdminCommunicationsCenterService.CreatePushNotificationResult result = adminCommunicationsCenterService.createPushNotification(
                adminId,
                request.recipientIds(),
                request.sendToAll(),
                request.title(),
                request.message(),
                request.priority(),
                request.scheduleAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/notifications/{itemId}")
    public ResponseEntity<AdminCommunicationsCenterService.CommunicationCardItem> getNotificationItem(
            @PathVariable UUID itemId,
            @RequestParam(required = false, defaultValue = "PUSH_NOTIFICATIONS") String tab
    ) {
        return ResponseEntity.ok(adminCommunicationsCenterService.getNotificationItem(itemId, tab));
    }

    @PatchMapping("/notifications/{itemId}")
    public ResponseEntity<Void> updateNotificationItem(
            @PathVariable UUID itemId,
            @RequestParam(required = false, defaultValue = "PUSH_NOTIFICATIONS") String tab,
            @RequestBody AdminUpdateNotificationRequest request
    ) {
        adminCommunicationsCenterService.updateNotificationItem(
                itemId,
                tab,
                request.title(),
                request.message(),
                request.priority(),
                request.sendNow()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notifications/{itemId}")
    public ResponseEntity<Void> deleteNotificationItem(
            @PathVariable UUID itemId,
            @RequestParam(required = false, defaultValue = "PUSH_NOTIFICATIONS") String tab
    ) {
        adminCommunicationsCenterService.deleteNotificationItem(itemId, tab);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/announcements")
    public ResponseEntity<UUID> createAnnouncement(@RequestBody AdminCreateAnnouncementRequest request) {
        UUID authorId = resolveAuthenticatedAdminActor(request.authorId(), "authorId");
        UUID announcementId = adminCommunicationsCenterService.createAnnouncement(
                authorId,
                request.title(),
                request.content(),
                request.priority(),
                request.expiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementId);
    }

    @GetMapping("/announcements/{announcementId}")
    public ResponseEntity<AdminCommunicationsCenterService.CommunicationCardItem> getAnnouncementItem(@PathVariable UUID announcementId) {
        return ResponseEntity.ok(adminCommunicationsCenterService.getAnnouncementItem(announcementId));
    }

    @PatchMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> updateAnnouncementItem(
            @PathVariable UUID announcementId,
            @RequestBody AdminUpdateAnnouncementRequest request
    ) {
        adminCommunicationsCenterService.updateAnnouncementItem(
                announcementId,
                request.title(),
                request.content(),
                request.priority(),
                request.expiresAt(),
                request.active()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncementItem(@PathVariable UUID announcementId) {
        adminCommunicationsCenterService.deleteAnnouncementItem(announcementId);
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

    private UUID resolveAuthenticatedAdminActor(UUID requestedActorId, String actorField) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        if (requestedActorId != null && !requestedActorId.equals(authenticatedUserId)) {
            throw new BusinessRuleException(actorField + " must match authenticated admin");
        }

        return authenticatedUserId;
    }

    public record AdminCreatePushNotificationRequest(
            UUID adminId,
            List<UUID> recipientIds,
            Boolean sendToAll,
            String title,
            @JsonAlias({"body", "content", "text"})
            String message,
            String priority,
            Instant scheduleAt
    ) {
    }

    public record AdminUpdateNotificationRequest(
            String title,
            @JsonAlias({"body", "content", "text"})
            String message,
            String priority,
            Boolean sendNow
    ) {
    }

    public record AdminCreateAnnouncementRequest(
            UUID authorId,
            String title,
            @JsonAlias({"body", "description"})
            String content,
            String priority,
            Instant expiresAt
    ) {
    }

    public record AdminUpdateAnnouncementRequest(
            String title,
            @JsonAlias({"body", "description"})
            String content,
            String priority,
            Instant expiresAt,
            Boolean active
    ) {
    }
}
