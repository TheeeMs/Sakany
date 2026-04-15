package com.theMs.sakany.property.internal.application.queries;

import com.theMs.sakany.property.internal.domain.Compound;
import com.theMs.sakany.property.internal.domain.CompoundRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetCompoundByIdQueryHandler implements QueryHandler<GetCompoundByIdQuery, Compound> {

    private final CompoundRepository compoundRepository;

    public GetCompoundByIdQueryHandler(CompoundRepository compoundRepository) {
        this.compoundRepository = compoundRepository;
    }

    @Override
    public Compound handle(GetCompoundByIdQuery query) {
        return compoundRepository.findById(query.id())
                .orElseThrow(() -> new NotFoundException("Compound", query.id().toString()));
    }
}
