-- V11: Modify Community Events to match UI
ALTER TABLE community_events
    RENAME COLUMN event_date TO start_date;

ALTER TABLE community_events
    ADD COLUMN end_date TIMESTAMPTZ,
    ADD COLUMN image_url VARCHAR(500),
    ADD COLUMN host_name VARCHAR(255),
    ADD COLUMN price DOUBLE PRECISION;
