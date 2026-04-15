package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.access.internal.domain.VisitLog;
import com.theMs.sakany.access.internal.domain.VisitLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogVisitorExitCommandHandler {

    private final VisitLogRepository repository;

    public LogVisitorExitCommandHandler(VisitLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void handle(LogVisitorExitCommand command) {
        VisitLog visitLog = repository.findById(command.visitLogId())
            .orElseThrow(() -> new IllegalArgumentException("Visit log not found: " + command.visitLogId()));

        visitLog.logExit();
        repository.save(visitLog);
    }
}
