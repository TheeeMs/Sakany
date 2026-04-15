-- V8: Create Community Events and Registrations Tables

CREATE TABLE community_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    event_date TIMESTAMPTZ NOT NULL,
    max_attendees INTEGER,
    current_attendees INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    approved_by UUID,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    resident_id UUID NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL,

    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_event_registration_event FOREIGN KEY (event_id) REFERENCES community_events(id)
);

CREATE UNIQUE INDEX uq_event_resident_registered ON event_registrations (event_id, resident_id) WHERE status = 'REGISTERED';
