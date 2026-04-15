package com.theMs.sakany.accounts.internal.api.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        UUID userId,
        String accessToken,
        String refreshToken
) {
}
