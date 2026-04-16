package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.access.internal.domain.AccessCode;
import org.springframework.stereotype.Component;

@Component
public class AccessCodeMapper {

    public AccessCode toDomain(AccessCodeEntity entity) {
        return AccessCode.rehydrate(
            entity.getId(),
            entity.getResidentId(),
            entity.getVisitorName(),
            entity.getVisitorPhone(),
            entity.getPurpose(),
            entity.getCode(),
            entity.getQrData(),
            entity.isSingleUse(),
            entity.getValidFrom(),
            entity.getValidUntil(),
            entity.getStatus(),
            entity.getUsedAt()
        );
    }

    public AccessCodeEntity toEntity(AccessCode domain) {
        AccessCodeEntity entity = new AccessCodeEntity(
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
        entity.setId(domain.getId());
        return entity;
    }
}
