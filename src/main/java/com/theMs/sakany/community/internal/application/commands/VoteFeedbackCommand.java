package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.VoteType;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record VoteFeedbackCommand(
    UUID feedbackId,
    UUID voterId,
    VoteType voteType
) implements Command<Void> {}
