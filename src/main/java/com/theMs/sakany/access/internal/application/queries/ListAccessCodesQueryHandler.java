package com.theMs.sakany.access.internal.application.queries;

import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListAccessCodesQueryHandler {

    private final AccessCodeRepository repository;

    public ListAccessCodesQueryHandler(AccessCodeRepository repository) {
        this.repository = repository;
    }

    public List<AccessCode> handle(ListAccessCodesQuery query) {
        return repository.findByResidentId(query.residentId());
    }
}
