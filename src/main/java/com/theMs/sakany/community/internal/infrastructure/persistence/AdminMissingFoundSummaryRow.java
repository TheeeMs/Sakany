package com.theMs.sakany.community.internal.infrastructure.persistence;

public interface AdminMissingFoundSummaryRow {
    Long getTotalCount();

    Long getMissingCount();

    Long getFoundCount();

    Long getOpenCount();

    Long getMatchedCount();

    Long getResolvedCount();
}
