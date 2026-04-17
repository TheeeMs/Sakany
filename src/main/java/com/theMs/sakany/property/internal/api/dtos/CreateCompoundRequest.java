package com.theMs.sakany.property.internal.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateCompoundRequest(
    @NotBlank(message = "Compound name is required")
    String name,

    @NotBlank(message = "Compound address is required")
    String address
) {
}
