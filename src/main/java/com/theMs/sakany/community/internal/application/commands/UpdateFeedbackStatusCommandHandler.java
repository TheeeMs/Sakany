package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateFeedbackStatusCommandHandler implements CommandHandler<UpdateFeedbackStatusCommand, Void> {

    private final FeedbackRepository feedbackRepository;

    public UpdateFeedbackStatusCommandHandler(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    @Transactional
    public Void handle(UpdateFeedbackStatusCommand command) {
        Feedback feedback = feedbackRepository.findById(command.feedbackId())
            .orElseThrow(() -> new NotFoundException("Feedback", command.feedbackId()));

        feedback.updateStatus(command.newStatus());
        feedbackRepository.save(feedback);
        return null;
    }
}
