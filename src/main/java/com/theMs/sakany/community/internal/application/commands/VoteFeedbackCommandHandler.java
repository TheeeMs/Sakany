package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.community.internal.domain.VoteType;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteFeedbackCommandHandler implements CommandHandler<VoteFeedbackCommand, Void> {

    private final FeedbackRepository feedbackRepository;

    public VoteFeedbackCommandHandler(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    @Transactional
    public Void handle(VoteFeedbackCommand command) {
        Feedback feedback = feedbackRepository.findById(command.feedbackId())
            .orElseThrow(() -> new NotFoundException("Feedback", command.feedbackId()));

        if (feedbackRepository.hasVoted(command.feedbackId(), command.voterId())) {
            throw new BusinessRuleException("User has already voted on this feedback");
        }

        if (command.voteType() == VoteType.UP) {
            feedback.upvote(command.voterId());
        } else {
            feedback.downvote(command.voterId());
        }

        feedbackRepository.recordVote(command.feedbackId(), command.voterId(), command.voteType());
        feedbackRepository.save(feedback);
        return null;
    }
}
