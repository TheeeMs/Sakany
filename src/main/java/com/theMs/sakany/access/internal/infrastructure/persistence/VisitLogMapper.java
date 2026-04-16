package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.access.internal.domain.VisitLog;
import org.springframework.stereotype.Component;

@Component
public class VisitLogMapper {

    public VisitLog toDomain(VisitLogEntity entity) {
        return VisitLog.rehydrate(
            entity.getId(),
            entity.getAccessCodeId(),
            entity.getResidentId(),
            entity.getVisitorName(),
            entity.getEntryTime(),
            entity.getExitTime(),
            entity.getGateNumber()
        );
    }

    public VisitLogEntity toEntity(VisitLog domain) {
        VisitLogEntity entity = new VisitLogEntity(
            domain.getAccessCodeId(),
            domain.getResidentId(),
            domain.getVisitorName(),
            domain.getEntryTime(),
            domain.getExitTime(),
            domain.getGateNumber()
        );
        entity.setId(domain.getId());
        return entity;
    }
}
