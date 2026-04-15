package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.FeedbackStatus;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record UpdateFeedbackStatusCommand(
    UUID feedbackId,
    FeedbackStatus newStatus
) implements Command<Void> {}
