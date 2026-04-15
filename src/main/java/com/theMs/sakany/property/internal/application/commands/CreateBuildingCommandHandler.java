package com.theMs.sakany.property.internal.application.commands;

import com.theMs.sakany.property.internal.domain.Building;
import com.theMs.sakany.property.internal.domain.BuildingRepository;
import com.theMs.sakany.property.internal.domain.CompoundRepository;
import com.theMs.sakany.shared.cqrs.CommandHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateBuildingCommandHandler implements CommandHandler<CreateBuildingCommand, UUID> {

    private final BuildingRepository buildingRepository;
    private final CompoundRepository compoundRepository;

    public CreateBuildingCommandHandler(BuildingRepository buildingRepository, CompoundRepository compoundRepository) {
        this.buildingRepository = buildingRepository;
        this.compoundRepository = compoundRepository;
    }

    @Override
    @Transactional
    public UUID handle(CreateBuildingCommand command) {
        compoundRepository.findById(command.compoundId())
            .orElseThrow(() -> new NotFoundException("Compound", command.compoundId().toString()));

        Building building = Building.create(
            command.compoundId(),
            command.name(),
            command.numberOfFloors()
        );

        Building savedBuilding = buildingRepository.save(building);

        return savedBuilding.getId();
    }
}
