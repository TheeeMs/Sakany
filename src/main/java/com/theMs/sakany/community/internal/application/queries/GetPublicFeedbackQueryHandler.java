package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetPublicFeedbackQueryHandler implements QueryHandler<GetPublicFeedbackQuery, List<Feedback>> {

    private final FeedbackRepository feedbackRepository;

    public GetPublicFeedbackQueryHandler(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public List<Feedback> handle(GetPublicFeedbackQuery query) {
        return feedbackRepository.findPublicFeedback();
    }
}
