package com.theMs.sakany.property.internal.api.dtos;

import java.util.UUID;

public record BuildingResponse(
    UUID id,
    UUID compoundId,
    String name,
    int numberOfFloors
) {
}
