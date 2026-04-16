package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import com.theMs.sakany.access.internal.application.services.AccessCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateAccessCodeCommandHandler {

    private final AccessCodeRepository repository;
    private final AccessCodeGenerator codeGenerator;

    public CreateAccessCodeCommandHandler(AccessCodeRepository repository, AccessCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public UUID handle(CreateAccessCodeCommand command) {
        String prefix = command.purpose().name().substring(0, 3).toUpperCase();
        String code = codeGenerator.generateCode(prefix);
        String qrData = code; // For now, QR data is just the code. Later: JWT or encryption

        AccessCode accessCode = AccessCode.create(
            command.residentId(),
            command.visitorName(),
            command.visitorPhone(),
            command.purpose(),
            code,
            qrData,
            command.isSingleUse(),
            command.validFrom(),
            command.validUntil()
        );

        repository.save(accessCode);
        return accessCode.getId();
    }
}
