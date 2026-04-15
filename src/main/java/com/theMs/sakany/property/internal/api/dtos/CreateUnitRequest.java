package com.theMs.sakany.property.internal.api.dtos;

import com.theMs.sakany.property.internal.domain.UnitType;
import java.util.UUID;

public record CreateUnitRequest(
    UUID buildingId,
    String unitNumber,
    int floor,
    UnitType type
) {
}
