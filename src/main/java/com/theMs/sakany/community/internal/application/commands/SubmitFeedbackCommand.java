package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.FeedbackType;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record SubmitFeedbackCommand(
    UUID authorId,
    String title,
    String content,
    FeedbackType type,
    boolean isPublic
) implements Command<UUID> {}
