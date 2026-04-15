package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;

public record GetPublicFeedbackQuery() implements Query<List<Feedback>> {}
