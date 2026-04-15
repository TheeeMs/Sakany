package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubmitFeedbackCommandHandler implements CommandHandler<SubmitFeedbackCommand, UUID> {

    private final FeedbackRepository feedbackRepository;

    public SubmitFeedbackCommandHandler(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    @Transactional
    public UUID handle(SubmitFeedbackCommand command) {
        Feedback feedback = Feedback.create(
            command.authorId(),
            command.title(),
            command.content(),
            command.type(),
            command.isPublic()
        );
        Feedback savedFeedback = feedbackRepository.save(feedback);
        return savedFeedback.getId();
    }
}
