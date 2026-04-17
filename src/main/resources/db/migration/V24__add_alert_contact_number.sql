ALTER TABLE alerts
ADD COLUMN IF NOT EXISTS contact_number VARCHAR(30);

CREATE INDEX IF NOT EXISTS idx_alerts_contact_number
ON alerts(contact_number);