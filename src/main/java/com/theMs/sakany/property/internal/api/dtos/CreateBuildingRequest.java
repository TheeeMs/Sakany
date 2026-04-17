package com.theMs.sakany.property.internal.api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBuildingRequest(
    @NotNull(message = "compoundId is required")
    UUID compoundId,

    @NotBlank(message = "Building name is required")
    String name,

    @NotNull(message = "numberOfFloors is required")
    @Min(value = 1, message = "numberOfFloors must be at least 1")
    Integer numberOfFloors
) {
}
