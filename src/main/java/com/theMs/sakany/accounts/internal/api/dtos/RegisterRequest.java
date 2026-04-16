package com.theMs.sakany.accounts.internal.api.dtos;

import com.theMs.sakany.accounts.internal.domain.LoginMethod;
import com.theMs.sakany.accounts.internal.domain.ResidentType;

import java.util.UUID;

public record RegisterRequest(
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String password,
        ResidentType type,
        UUID unitId,
        LoginMethod loginMethod
) {
}
