-- V20: Support admin create-event form fields

ALTER TABLE community_events
    ADD COLUMN tags TEXT,
    ADD COLUMN recurring_event BOOLEAN NOT NULL DEFAULT FALSE;
