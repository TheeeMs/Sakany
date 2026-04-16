package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.access.internal.application.services.AccessCodeGenerator;
import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReactivateAccessCodeCommandHandler {

    private final AccessCodeRepository repository;
    private final AccessCodeGenerator codeGenerator;

    public ReactivateAccessCodeCommandHandler(AccessCodeRepository repository, AccessCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public UUID handle(ReactivateAccessCodeCommand command) {
        AccessCode oldAccessCode = repository.findById(command.existingAccessCodeId())
                .orElseThrow(() -> new IllegalArgumentException("Access code not found"));

        if (!oldAccessCode.getResidentId().equals(command.residentId())) {
            throw new IllegalArgumentException("Unauthorized to reactivate this access code");
        }

        String prefix = oldAccessCode.getPurpose().name().substring(0, 3).toUpperCase();
        String code = codeGenerator.generateCode(prefix);
        String qrData = code; // For now, QR data is just the code

        AccessCode newAccessCode = AccessCode.create(
                command.residentId(),
                oldAccessCode.getVisitorName(),
                oldAccessCode.getVisitorPhone(),
                oldAccessCode.getPurpose(),
                code,
                qrData,
                oldAccessCode.isSingleUse(),
                command.validFrom(),
                command.validUntil()
        );

            AccessCode saved = repository.save(newAccessCode);
            return saved.getId();
    }
}