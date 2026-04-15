package com.theMs.sakany.accounts.internal.api.dtos;

public record LoginEmailRequest(
        String email,
        String password
) {
}
