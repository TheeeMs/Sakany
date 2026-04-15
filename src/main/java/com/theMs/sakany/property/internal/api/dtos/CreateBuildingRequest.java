package com.theMs.sakany.property.internal.api.dtos;

import java.util.UUID;

public record CreateBuildingRequest(
    UUID compoundId,
    String name,
    int numberOfFloors
) {
}
