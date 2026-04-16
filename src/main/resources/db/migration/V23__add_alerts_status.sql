-- V23: Add explicit workflow status for alerts (OPEN, MATCHED, RESOLVED)

ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

UPDATE alerts
SET status = CASE
    WHEN is_resolved = TRUE THEN 'RESOLVED'
    ELSE 'OPEN'
END
WHERE status IS NULL;

ALTER TABLE alerts
    ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_alert_status'
    ) THEN
        ALTER TABLE alerts
            ADD CONSTRAINT chk_alert_status
            CHECK (status IN ('OPEN', 'MATCHED', 'RESOLVED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_alerts_status ON alerts(status);
CREATE INDEX IF NOT EXISTS idx_alerts_type_status ON alerts(type, status);