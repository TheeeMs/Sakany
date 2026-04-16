package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Feedback;
import com.theMs.sakany.shared.cqrs.Query;

import java.util.List;
import java.util.UUID;

public record GetMyFeedbackQuery(UUID authorId) implements Query<List<Feedback>> {}