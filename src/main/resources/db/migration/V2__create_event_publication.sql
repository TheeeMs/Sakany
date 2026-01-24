-- V2: Create Spring Modulith Event Publication Table
-- Required for the "Outbox Pattern" implementation in Spring Modulith
-- This table stores domain events before they are processed/published

CREATE TABLE event_publication (
  id UUID NOT NULL,
  completion_date TIMESTAMPTZ,
  event_type VARCHAR(512),
  listener_id VARCHAR(512),
  publication_date TIMESTAMPTZ,
  serialized_event VARCHAR(4000),
  PRIMARY KEY (id)
);
