package com.theMs.sakany.access.internal.application.queries;

import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAccessCodeQueryHandler {

    private final AccessCodeRepository repository;

    public GetAccessCodeQueryHandler(AccessCodeRepository repository) {
        this.repository = repository;
    }

    public AccessCode handle(GetAccessCodeQuery query) {
        return repository.findById(query.id())
            .orElseThrow(() -> new IllegalArgumentException("Access code not found: " + query.id()));
    }
}
