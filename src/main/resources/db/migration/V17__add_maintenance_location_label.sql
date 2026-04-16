ALTER TABLE maintenance_requests
ADD COLUMN IF NOT EXISTS location_label VARCHAR(255);

UPDATE maintenance_requests mr
SET location_label = CONCAT('Unit ', u.unit_number)
FROM units u
WHERE mr.unit_id = u.id
  AND (mr.location_label IS NULL OR mr.location_label = '');

CREATE INDEX IF NOT EXISTS idx_maintenance_requests_location_label
ON maintenance_requests(location_label);
