package com.theMs.sakany.property.internal.api.dtos;

import java.util.UUID;

public record CompoundResponse(
    UUID id,
    String name,
    String address
) {
}
