package com.theMs.sakany.community.internal.api.controllers;

import com.theMs.sakany.community.internal.application.commands.SubmitFeedbackCommand;
import com.theMs.sakany.community.internal.application.commands.UpdateFeedbackStatusCommand;
import com.theMs.sakany.community.internal.application.commands.VoteFeedbackCommand;
import com.theMs.sakany.community.internal.application.queries.GetPublicFeedbackQuery;
import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackStatus;
import com.theMs.sakany.community.internal.domain.FeedbackType;
import com.theMs.sakany.community.internal.domain.VoteType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public FeedbackController(
        com.theMs.sakany.shared.cqrs.CommandHandler<SubmitFeedbackCommand, UUID> submitFeedbackHandler,
        com.theMs.sakany.shared.cqrs.CommandHandler<VoteFeedbackCommand, Void> voteFeedbackHandler,
        com.theMs.sakany.shared.cqrs.CommandHandler<UpdateFeedbackStatusCommand, Void> updateFeedbackStatusHandler,
        com.theMs.sakany.shared.cqrs.QueryHandler<GetPublicFeedbackQuery, List<Feedback>> getPublicFeedbackHandler
    ) {
        this.submitFeedbackHandler = submitFeedbackHandler;
        this.voteFeedbackHandler = voteFeedbackHandler;
        this.updateFeedbackStatusHandler = updateFeedbackStatusHandler;
        this.getPublicFeedbackHandler = getPublicFeedbackHandler;
    }

    public record SubmitFeedbackRequest(
        UUID authorId,
        String title,
        String content,
        FeedbackType type,
        boolean isPublic
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
        int downvotes
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
                feedback.getDownvotes()
            );
        }
    }

    public record VoteFeedbackRequest(UUID voterId, VoteType voteType) {}

    public record UpdateFeedbackStatusRequest(FeedbackStatus newStatus) {}

    @PostMapping
    public ResponseEntity<UUID> submitFeedback(@RequestBody SubmitFeedbackRequest request) {
        UUID feedbackId = submitFeedbackHandler.handle(new SubmitFeedbackCommand(
            request.authorId(),
            request.title(),
            request.content(),
            request.type(),
            request.isPublic()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackId);
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getPublicFeedback() {
        List<Feedback> feedbackList = getPublicFeedbackHandler.handle(new GetPublicFeedbackQuery());
        List<FeedbackResponse> response = feedbackList.stream().map(FeedbackResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> voteFeedback(@PathVariable UUID id, @RequestBody VoteFeedbackRequest request) {
        voteFeedbackHandler.handle(new VoteFeedbackCommand(id, request.voterId(), request.voteType()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateFeedbackStatus(@PathVariable UUID id, @RequestBody UpdateFeedbackStatusRequest request) {
        updateFeedbackStatusHandler.handle(new UpdateFeedbackStatusCommand(id, request.newStatus()));
        return ResponseEntity.noContent().build();
    }
}
