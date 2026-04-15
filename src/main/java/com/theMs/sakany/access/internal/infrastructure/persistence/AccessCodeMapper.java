package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.access.internal.domain.AccessCode;
import org.springframework.stereotype.Component;

@Component
public class AccessCodeMapper {

    public AccessCode toDomain(AccessCodeEntity entity) {
        return AccessCode.create(
            entity.getResidentId(),
            entity.getVisitorName(),
            entity.getVisitorPhone(),
            entity.getPurpose(),
            entity.getCode(),
            entity.getQrData(),
            entity.isSingleUse(),
            entity.getValidFrom(),
            entity.getValidUntil()
        );
    }

    public AccessCodeEntity toEntity(AccessCode domain) {
        return new AccessCodeEntity(
            domain.getResidentId(),
            domain.getVisitorName(),
            domain.getVisitorPhone(),
            domain.getPurpose(),
            domain.getCode(),
            domain.getQrData(),
            domain.isSingleUse(),
            domain.getValidFrom(),
            domain.getValidUntil(),
            domain.getStatus(),
            domain.getUsedAt()
        );
    }
}
