package com.theMs.sakany.property.internal.application.commands;

import com.theMs.sakany.property.internal.domain.Compound;
import com.theMs.sakany.property.internal.domain.CompoundRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateCompoundCommandHandler implements CommandHandler<CreateCompoundCommand, UUID> {

    private final CompoundRepository compoundRepository;

    public CreateCompoundCommandHandler(CompoundRepository compoundRepository) {
        this.compoundRepository = compoundRepository;
    }

    @Override
    @Transactional
    public UUID handle(CreateCompoundCommand command) {
        Compound compound = Compound.create(
            command.name(),
            command.address()
        );

        Compound savedCompound = compoundRepository.save(compound);

        return savedCompound.getId();
    }
}
