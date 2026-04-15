package com.theMs.sakany.property.internal.application.queries;

import com.theMs.sakany.property.internal.domain.Building;
import com.theMs.sakany.property.internal.domain.BuildingRepository;
import com.theMs.sakany.property.internal.domain.CompoundRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetBuildingsByCompoundQueryHandler implements QueryHandler<GetBuildingsByCompoundQuery, List<Building>> {

    private final BuildingRepository buildingRepository;
    private final CompoundRepository compoundRepository;

    public GetBuildingsByCompoundQueryHandler(BuildingRepository buildingRepository, CompoundRepository compoundRepository) {
        this.buildingRepository = buildingRepository;
        this.compoundRepository = compoundRepository;
    }

    @Override
    public List<Building> handle(GetBuildingsByCompoundQuery query) {
        compoundRepository.findById(query.compoundId())
            .orElseThrow(() -> new NotFoundException("Compound", query.compoundId().toString()));

        return buildingRepository.findByCompoundId(query.compoundId());
    }
}
