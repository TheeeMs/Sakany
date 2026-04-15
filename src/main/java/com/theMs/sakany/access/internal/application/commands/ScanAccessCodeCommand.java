package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;

import java.util.UUID;

public record ScanAccessCodeCommand(
    String code,
    String gateNumber
) implements Command {
}
