package com.theMs.sakany.access.internal.application.commands;

import com.theMs.sakany.access.internal.domain.AccessCode;
import com.theMs.sakany.access.internal.domain.AccessCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeAccessCodeCommandHandler {

    private final AccessCodeRepository repository;

    public RevokeAccessCodeCommandHandler(AccessCodeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void handle(RevokeAccessCodeCommand command) {
        AccessCode accessCode = repository.findById(command.accessCodeId())
            .orElseThrow(() -> new IllegalArgumentException("Access code not found: " + command.accessCodeId()));

        accessCode.revoke();
        repository.save(accessCode);
    }
}
