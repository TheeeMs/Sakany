CREATE TABLE IF NOT EXISTS maintenance_timeline_events (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES maintenance_requests(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    details TEXT,
    actor_id UUID,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_maintenance_timeline_events_request_id
ON maintenance_timeline_events(request_id);

CREATE INDEX IF NOT EXISTS idx_maintenance_timeline_events_created_at
ON maintenance_timeline_events(created_at);

INSERT INTO maintenance_timeline_events (id, request_id, event_type, title, details, actor_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    mr.id,
    'REQUEST_SUBMITTED',
    'Request Submitted',
    NULL,
    mr.resident_id,
    mr.created_at,
    mr.created_at
FROM maintenance_requests mr
WHERE NOT EXISTS (
    SELECT 1
    FROM maintenance_timeline_events mte
    WHERE mte.request_id = mr.id
      AND mte.event_type = 'REQUEST_SUBMITTED'
);
