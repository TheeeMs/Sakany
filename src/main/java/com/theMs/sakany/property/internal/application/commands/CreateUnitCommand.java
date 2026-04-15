package com.theMs.sakany.property.internal.application.commands;

import com.theMs.sakany.property.internal.domain.UnitType;
import com.theMs.sakany.shared.cqrs.Command;
import java.util.UUID;

public record CreateUnitCommand(
    UUID buildingId,
    String unitNumber,
    int floor,
    UnitType type
) implements Command<UUID> {
}
