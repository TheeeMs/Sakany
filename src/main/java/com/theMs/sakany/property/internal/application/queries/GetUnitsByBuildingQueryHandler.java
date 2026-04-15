package com.theMs.sakany.property.internal.application.queries;

import com.theMs.sakany.property.internal.domain.BuildingRepository;
import com.theMs.sakany.property.internal.domain.Unit;
import com.theMs.sakany.property.internal.domain.UnitRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import com.theMs.sakany.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetUnitsByBuildingQueryHandler implements QueryHandler<GetUnitsByBuildingQuery, List<Unit>> {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;

    public GetUnitsByBuildingQueryHandler(UnitRepository unitRepository, BuildingRepository buildingRepository) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    public List<Unit> handle(GetUnitsByBuildingQuery query) {
        buildingRepository.findById(query.buildingId())
            .orElseThrow(() -> new NotFoundException("Building", query.buildingId().toString()));

        return unitRepository.findByBuildingId(query.buildingId());
    }
}
