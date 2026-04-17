-- Prevent duplicate unit numbers inside the same building.
-- Uses normalized comparison (trim + lower) to block case/whitespace variants.
CREATE UNIQUE INDEX ux_units_building_unit_number_norm
    ON units (building_id, lower(btrim(unit_number)));
