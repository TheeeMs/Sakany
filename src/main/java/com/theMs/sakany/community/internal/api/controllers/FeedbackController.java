package com.theMs.sakany.community.internal.api.controllers;

import com.theMs.sakany.community.internal.application.commands.SubmitFeedbackCommand;
import com.theMs.sakany.community.internal.application.commands.UpdateFeedbackStatusCommand;
import com.theMs.sakany.community.internal.application.commands.VoteFeedbackCommand;
import com.theMs.sakany.community.internal.application.queries.GetMyFeedbackQuery;
import com.theMs.sakany.community.internal.application.queries.GetPublicFeedbackQuery;
import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.community.internal.domain.FeedbackStatus;
import com.theMs.sakany.community.internal.domain.FeedbackType;
import com.theMs.sakany.community.internal.domain.VoteType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/feedback")
public class FeedbackController {

    private final com.theMs.sakany.shared.cqrs.CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler;
    private final com.theMs.sakany.shared.cqrs.CommandHandler<VoteFeedbackCommand, Void> voteFeedbackHandler;
    private final com.theMs.sakany.shared.cqrs.CommandHandler<UpdateFeedbackStatusCommand, Void> updateFeedbackStatusHandler;
    private final com.theMs.sakany.shared.cqrs.QueryHandler<GetPublicFeedbackQuery, List<Feedback>> getPublicFeedbackHandler;
    private final com.theMs.sakany.shared.cqrs.QueryHandler<GetMyFeedbackQuery, List<Feedback>> getMyFeedbackHandler;
    private final FeedbackRepository feedbackRepository;

    public FeedbackController(
        com.theMs.sakany.shared.cqrs.CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler,
        com.theMs.sakany.shared.cqrs.CommandHandler<VoteFeedbackCommand, Void> voteFeedbackHandler,
        com.theMs.sakany.shared.cqrs.CommandHandler<UpdateFeedbackStatusCommand, Void> updateFeedbackStatusHandler,
        com.theMs.sakany.shared.cqrs.QueryHandler<GetPublicFeedbackQuery, List<Feedback>> getPublicFeedbackHandler,
        com.theMs.sakany.shared.cqrs.QueryHandler<GetMyFeedbackQuery, List<Feedback>> getMyFeedbackHandler,
        FeedbackRepository feedbackRepository
    ) {
        this.submitFeedbackHandler = submitFeedbackHandler;
        this.voteFeedbackHandler = voteFeedbackHandler;
        this.updateFeedbackStatusHandler = updateFeedbackStatusHandler;
        this.getPublicFeedbackHandler = getPublicFeedbackHandler;
        this.getMyFeedbackHandler = getMyFeedbackHandler;
        this.feedbackRepository = feedbackRepository;
    }

    public record SubmitFeedbackRequest(
        String title,
        String content,
        FeedbackType type,
        boolean isPublic,
        String category,
        String location,
        boolean isAnonymous,
        String imageUrl
    ) {}

    public record FeedbackResponse(
        UUID id,
        UUID authorId,
        String title,
        String content,
        FeedbackType type,
        boolean isPublic,
        FeedbackStatus status,
        int upvotes,
        int downvotes,
        String category,
        String location,
        boolean isAnonymous,
        String adminResponse,
        String imageUrl,
        int viewCount,
        Instant createdAt
    ) {
        public static FeedbackResponse from(Feedback feedback) {
            return new FeedbackResponse(
                feedback.getId(),
                feedback.getAuthorId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getType(),
                feedback.isPublic(),
                feedback.getStatus(),
                feedback.getUpvotes(),
                feedback.getDownvotes(),
                feedback.getCategory(),
                feedback.getLocation(),
                feedback.isAnonymous(),
                feedback.getAdminResponse(),
                feedback.getImageUrl(),
                feedback.getViewCount(),
                feedback.getCreatedAt()
            );
        }
    }

    public record MyFeedbackSummaryResponse(
        int totalPosts,
        int approvedPosts,
        int totalVotes,
        List<FeedbackResponse> posts
    ) {}

    public record VoteFeedbackRequest(VoteType voteType) {}

    public record UpdateFeedbackStatusRequest(FeedbackStatus newStatus) {}

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessRuleException("No authenticated user");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        } else {
            return UUID.fromString(principal.toString());
        }
    }

    private boolean isAdminActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    @PostMapping
    public ResponseEntity<UUID> submitFeedback(@RequestBody SubmitFeedbackRequest request) {
        UUID authorId = getAuthenticatedUserId();
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
    public ResponseEntity<List<FeedbackResponse>> getPublicFeedback() {
        List<Feedback> feedbackList = getPublicFeedbackHandler.handle(new GetPublicFeedbackQuery());
        List<FeedbackResponse> response = feedbackList.stream().map(FeedbackResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MyFeedbackSummaryResponse> getMyFeedback() {
        UUID userId = getAuthenticatedUserId();
        List<Feedback> myFeedbackList = getMyFeedbackHandler.handle(new GetMyFeedbackQuery(userId));
        List<FeedbackResponse> posts = myFeedbackList.stream().map(FeedbackResponse::from).collect(Collectors.toList());
        
        int totalPosts = posts.size();
        int approvedPosts = (int) myFeedbackList.stream().filter(f -> f.getStatus() == FeedbackStatus.APPROVED).count();
        int totalVotes = myFeedbackList.stream().mapToInt(f -> f.getUpvotes() + f.getDownvotes()).sum();

        return ResponseEntity.ok(new MyFeedbackSummaryResponse(totalPosts, approvedPosts, totalVotes, posts));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> voteFeedback(@PathVariable UUID id, @RequestBody VoteFeedbackRequest request) {
        UUID voterId = getAuthenticatedUserId();
        voteFeedbackHandler.handle(new VoteFeedbackCommand(id, voterId, request.voteType()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateFeedbackStatus(@PathVariable UUID id, @RequestBody UpdateFeedbackStatusRequest request) {
        UUID actorId = getAuthenticatedUserId();
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Feedback", id));

        if (!isAdminActor() && !actorId.equals(feedback.getAuthorId())) {
            throw new BusinessRuleException("You are not allowed to modify this feedback");
        }

        updateFeedbackStatusHandler.handle(new UpdateFeedbackStatusCommand(id, request.newStatus()));
        return ResponseEntity.noContent().build();
    }
}
