package com.theMs.sakany.community.internal.application.queries;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetActiveAlertsQueryHandler implements QueryHandler<GetActiveAlertsQuery, List<Alert>> {

    private final AlertRepository alertRepository;

    public GetActiveAlertsQueryHandler(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<Alert> handle(GetActiveAlertsQuery query) {
        return alertRepository.findActiveAlerts();
    }
}
