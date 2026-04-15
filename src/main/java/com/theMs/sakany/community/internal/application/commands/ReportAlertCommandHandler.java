package com.theMs.sakany.community.internal.application.commands;

import com.theMs.sakany.community.internal.domain.Alert;
import com.theMs.sakany.community.internal.domain.AlertRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReportAlertCommandHandler implements CommandHandler<ReportAlertCommand, UUID> {

    private final AlertRepository alertRepository;

    public ReportAlertCommandHandler(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional
    public UUID handle(ReportAlertCommand command) {
        Alert alert = Alert.create(
            command.reporterId(),
            command.type(),
            command.title(),
            command.description(),
            command.photoUrls()
        );
        Alert savedAlert = alertRepository.save(alert);
        return savedAlert.getId();
    }
}
