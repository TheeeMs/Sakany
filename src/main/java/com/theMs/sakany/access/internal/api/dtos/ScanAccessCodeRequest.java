package com.theMs.sakany.access.internal.api.dtos;

public record ScanAccessCodeRequest(
    String code,
    String gateNumber
) {
}
