package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.AnnouncementPriority;
import com.theMs.sakany.shared.cqrs.Command;

import java.time.Instant;
import java.util.UUID;

public record CreateAnnouncementCommand(
    UUID authorId,
    String title,
    String content,
    AnnouncementPriority priority,
    Instant expiresAt
) implements Command<UUID> {}
