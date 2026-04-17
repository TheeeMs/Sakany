package com.theMs.sakany.property.internal.api.dtos;

import com.theMs.sakany.property.internal.domain.UnitType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateUnitRequest(
    @NotNull(message = "buildingId is required")
    UUID buildingId,

    @NotBlank(message = "unitNumber is required")
    String unitNumber,

    @NotNull(message = "floor is required")
    @Min(value = 0, message = "floor cannot be negative")
    Integer floor,

    @NotNull(message = "type is required")
    UnitType type
) {
}
