package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import com.theMs.sakany.access.internal.domain.VisitLog;
import com.theMs.sakany.access.internal.domain.VisitLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ScanAccessCodeCommandHandler {

    private final AccessCodeRepository accessCodeRepository;
    private final VisitLogRepository visitLogRepository;

    public ScanAccessCodeCommandHandler(
        AccessCodeRepository accessCodeRepository,
        VisitLogRepository visitLogRepository
    ) {
        this.accessCodeRepository = accessCodeRepository;
        this.visitLogRepository = visitLogRepository;
    }

    @Transactional
    public UUID handle(ScanAccessCodeCommand command) {
        AccessCode accessCode = accessCodeRepository.findByCode(command.code())
            .orElseThrow(() -> new IllegalArgumentException("Access code not found: " + command.code()));

        // Validate and use the access code
        accessCode.use();

        // Save the updated access code
        accessCodeRepository.save(accessCode);

        // Create and save visit log
        VisitLog visitLog = VisitLog.create(
            accessCode.getId(),
            accessCode.getResidentId(),
            accessCode.getVisitorName(),
            command.gateNumber()
        );

        visitLogRepository.save(visitLog);

        return visitLog.getId();
    }
}
