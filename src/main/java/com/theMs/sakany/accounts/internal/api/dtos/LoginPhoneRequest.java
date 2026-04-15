package com.theMs.sakany.accounts.internal.api.dtos;

public record LoginPhoneRequest(
        String phoneNumber,
        String otpCode
) {
}
