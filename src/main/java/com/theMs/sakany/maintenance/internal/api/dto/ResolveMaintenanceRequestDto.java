package com.theMs.sakany.maintenance.internal.api.dto;

import java.math.BigDecimal;

public record ResolveMaintenanceRequestDto(
        String resolution,
        BigDecimal totalCost
) {
}
