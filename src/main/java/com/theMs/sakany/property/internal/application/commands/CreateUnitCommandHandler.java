package com.theMs.sakany.property.internal.application.commands;

import com.theMs.sakany.property.internal.domain.BuildingRepository;
import com.theMs.sakany.property.internal.domain.Unit;
import com.theMs.sakany.property.internal.domain.UnitRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.BusinessRuleException;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateUnitCommandHandler implements CommandHandler<CreateUnitCommand, UUID> {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;

    public CreateUnitCommandHandler(UnitRepository unitRepository, BuildingRepository buildingRepository) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional
    public UUID handle(CreateUnitCommand command) {
        buildingRepository.findById(command.buildingId())
            .orElseThrow(() -> new NotFoundException("Building", command.buildingId().toString()));

        String normalizedUnitNumber = command.unitNumber() == null ? null : command.unitNumber().trim();
        if (normalizedUnitNumber != null
                && !normalizedUnitNumber.isEmpty()
                && unitRepository.existsByBuildingIdAndUnitNumber(command.buildingId(), normalizedUnitNumber)) {
            throw new BusinessRuleException("Unit number already exists in this building");
        }

        Unit unit = Unit.create(
            command.buildingId(),
            command.unitNumber(),
            command.floor(),
            command.type()
        );

        Unit savedUnit = unitRepository.save(unit);

        return savedUnit.getId();
    }
}
