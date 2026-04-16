ALTER TABLE resident_profiles
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

UPDATE resident_profiles rp
SET approval_status = CASE
    WHEN u.is_phone_verified THEN 'APPROVED'
    ELSE 'PENDING'
END
FROM users u
WHERE rp.user_id = u.id
  AND rp.approval_status = 'PENDING';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_resident_profiles_approval_status'
    ) THEN
        ALTER TABLE resident_profiles
        ADD CONSTRAINT chk_resident_profiles_approval_status
        CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_resident_profiles_approval_status
ON resident_profiles(approval_status);
