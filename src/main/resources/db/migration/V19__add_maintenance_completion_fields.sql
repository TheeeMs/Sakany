ALTER TABLE maintenance_requests
ADD COLUMN IF NOT EXISTS resolution_notes TEXT;

ALTER TABLE maintenance_requests
ADD COLUMN IF NOT EXISTS resolution_cost DECIMAL(12,2);

CREATE INDEX IF NOT EXISTS idx_maintenance_requests_resolved_at
ON maintenance_requests(resolved_at);
