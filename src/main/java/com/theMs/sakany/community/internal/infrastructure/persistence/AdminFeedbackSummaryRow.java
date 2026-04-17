package com.theMs.sakany.community.internal.infrastructure.persistence;

public interface AdminFeedbackSummaryRow {
    Long getPublicSuggestionsCount();

    Long getPrivateFeedbackCount();

    Long getTotalSuggestions();

    Long getPendingReviewCount();

    Long getTotalVotes();

    Long getPopularCount();
}
