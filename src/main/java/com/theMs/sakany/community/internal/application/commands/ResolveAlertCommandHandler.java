package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResolveAlertCommandHandler implements CommandHandler<ResolveAlertCommand, Void> {

    private final AlertRepository alertRepository;

    public ResolveAlertCommandHandler(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional
    public Void handle(ResolveAlertCommand command) {
        Alert alert = alertRepository.findById(command.alertId())
            .orElseThrow(() -> new NotFoundException("Alert", command.alertId()));

        // In a real app we might verify if requestingUserId is the reporter or an admin.
        // For now, let's just make sure only the reporter can resolve it
        if (!alert.getReporterId().equals(command.requestingUserId())) {
            throw new BusinessRuleException("Only the reporter can resolve this alert");
        }

        alert.resolve();
        alertRepository.save(alert);
        return null;
    }
}
