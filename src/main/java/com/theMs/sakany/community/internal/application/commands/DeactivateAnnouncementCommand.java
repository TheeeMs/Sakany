package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record DeactivateAnnouncementCommand(
    UUID announcementId,
    UUID requestingUserId
) implements Command<Void> {}
