package com.theMs.sakany.property.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record CreateCompoundCommand(
    String name,
    String address
) implements Command<UUID> {
}
