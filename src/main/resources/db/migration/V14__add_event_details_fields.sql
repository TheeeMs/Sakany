-- V12: Add missing fields for Event Details screen

ALTER TABLE community_events
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN host_role VARCHAR(100),
    ADD COLUMN contact_phone VARCHAR(50),
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;
