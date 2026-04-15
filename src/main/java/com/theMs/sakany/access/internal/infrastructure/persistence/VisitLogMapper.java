package com.theMs.sakany.access.internal.infrastructure.persistence;

import com.theMs.sakany.access.internal.domain.VisitLog;
import org.springframework.stereotype.Component;

@Component
public class VisitLogMapper {

    public VisitLog toDomain(VisitLogEntity entity) {
        return VisitLog.create(
            entity.getAccessCodeId(),
            entity.getResidentId(),
            entity.getVisitorName(),
            entity.getGateNumber()
        );
    }

    public VisitLogEntity toEntity(VisitLog domain) {
        return new VisitLogEntity(
            domain.getAccessCodeId(),
            domain.getResidentId(),
            domain.getVisitorName(),
            domain.getEntryTime(),
            domain.getExitTime(),
            domain.getGateNumber()
        );
    }
}
