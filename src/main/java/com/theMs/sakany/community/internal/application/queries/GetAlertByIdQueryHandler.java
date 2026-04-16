package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAlertByIdQueryHandler implements QueryHandler<GetAlertByIdQuery, Optional<Alert>> {

    private final AlertRepository alertRepository;

    public GetAlertByIdQueryHandler(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public Optional<Alert> handle(GetAlertByIdQuery query) {
        return alertRepository.findById(query.id());
    }
}