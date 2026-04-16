package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.community.internal.domain.FeedbackRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetMyFeedbackQueryHandler implements QueryHandler<GetMyFeedbackQuery, List<Feedback>> {

    private final FeedbackRepository feedbackRepository;

    public GetMyFeedbackQueryHandler(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public List<Feedback> handle(GetMyFeedbackQuery query) {
        return feedbackRepository.findByAuthorId(query.authorId());
    }
}