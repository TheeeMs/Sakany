package com.theMs.sakany.accounts.internal.api.dtos;

import com.theMs.sakany.accounts.internal.domain.LoginMethod;

public record RegisterRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        LoginMethod loginMethod
) {
}
