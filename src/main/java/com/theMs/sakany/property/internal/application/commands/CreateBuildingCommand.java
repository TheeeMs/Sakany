package com.theMs.sakany.property.internal.application.commands;

import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record CreateBuildingCommand(
    UUID compoundId,
    String name,
    int numberOfFloors
) implements Command<UUID> {
}
