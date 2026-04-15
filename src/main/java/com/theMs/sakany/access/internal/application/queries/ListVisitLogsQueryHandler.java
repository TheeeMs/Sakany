package com.theMs.sakany.access.internal.application.queries;

import com.theMs.sakany.access.internal.domain.VisitLog;
import com.theMs.sakany.access.internal.domain.VisitLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListVisitLogsQueryHandler {

    private final VisitLogRepository repository;

    public ListVisitLogsQueryHandler(VisitLogRepository repository) {
        this.repository = repository;
    }

    public List<VisitLog> handle(ListVisitLogsQuery query) {
        return repository.findByResidentId(query.residentId());
    }
}
