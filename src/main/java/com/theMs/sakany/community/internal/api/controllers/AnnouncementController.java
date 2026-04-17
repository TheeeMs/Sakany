package com.theMs.sakany.community.internal.api.controllers;

import com.theMs.sakany.community.internal.application.commands.CreateAnnouncementCommand;
import com.theMs.sakany.community.internal.application.commands.DeactivateAnnouncementCommand;
import com.theMs.sakany.community.internal.application.queries.GetActiveAnnouncementsQuery;
import com.theMs.sakany.community.internal.domain.Announcement;
import com.theMs.sakany.community.internal.domain.AnnouncementPriority;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/announcements")
public class AnnouncementController {

    private final com.theMs.sakany.shared.cqrs.CommandHandler<CreateAnnouncementCommand, UUID> createAnnouncementHandler;
    private final com.theMs.sakany.shared.cqrs.CommandHandler<DeactivateAnnouncementCommand, Void> deactivateAnnouncementHandler;
    private final com.theMs.sakany.shared.cqrs.QueryHandler<GetActiveAnnouncementsQuery, List<Announcement>> getActiveAnnouncementsHandler;

    public AnnouncementController(
        com.theMs.sakany.shared.cqrs.CommandHandler<CreateAnnouncementCommand, UUID> createAnnouncementHandler,
        com.theMs.sakany.shared.cqrs.CommandHandler<DeactivateAnnouncementCommand, Void> deactivateAnnouncementHandler,
        com.theMs.sakany.shared.cqrs.QueryHandler<GetActiveAnnouncementsQuery, List<Announcement>> getActiveAnnouncementsHandler
    ) {
        this.createAnnouncementHandler = createAnnouncementHandler;
        this.deactivateAnnouncementHandler = deactivateAnnouncementHandler;
        this.getActiveAnnouncementsHandler = getActiveAnnouncementsHandler;
    }

    public record CreateAnnouncementRequest(
        UUID authorId,
        String title,
        String content,
        AnnouncementPriority priority,
        Instant expiresAt
    ) {}

    public record AnnouncementResponse(
        UUID id,
        UUID authorId,
        String title,
        String content,
        AnnouncementPriority priority,
        boolean isActive,
        Instant expiresAt
    ) {
        public static AnnouncementResponse from(Announcement announcement) {
            return new AnnouncementResponse(
                announcement.getId(),
                announcement.getAuthorId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getPriority(),
                announcement.isActive(),
                announcement.getExpiresAt()
            );
        }
    }

    public record DeactivateAnnouncementRequest(UUID requestingUserId) {}

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(principal.toString());
    }

    @PostMapping
    public ResponseEntity<UUID> createAnnouncement(@RequestBody CreateAnnouncementRequest request) {
        UUID authorId = getAuthenticatedUserId();
        UUID announcementId = createAnnouncementHandler.handle(new CreateAnnouncementCommand(
            authorId,
            request.title(),
            request.content(),
            request.priority(),
            request.expiresAt()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementId);
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getActiveAnnouncements() {
        List<Announcement> announcements = getActiveAnnouncementsHandler.handle(new GetActiveAnnouncementsQuery());
        List<AnnouncementResponse> response = announcements.stream().map(AnnouncementResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateAnnouncement(@PathVariable UUID id, @RequestBody(required = false) DeactivateAnnouncementRequest request) {
        UUID requestingUserId = getAuthenticatedUserId();
        deactivateAnnouncementHandler.handle(new DeactivateAnnouncementCommand(id, requestingUserId));
        return ResponseEntity.noContent().build();
    }
}
