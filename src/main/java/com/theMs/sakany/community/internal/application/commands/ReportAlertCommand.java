package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.community.internal.domain.AlertCategory;
import com.theMs.sakany.shared.cqrs.Command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportAlertCommand(
    UUID reporterId,
    AlertType type,
    AlertCategory category,
    String title,
    String description,
    String location,
    Instant eventTime,
    List<String> photoUrls
) implements Command<UUID> {}
