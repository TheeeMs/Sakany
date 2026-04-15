package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.AlertType;
import com.theMs.sakany.shared.cqrs.Command;

import java.util.List;
import java.util.UUID;

public record ReportAlertCommand(
    UUID reporterId,
    AlertType type,
    String title,
    String description,
    List<String> photoUrls
) implements Command<UUID> {}
