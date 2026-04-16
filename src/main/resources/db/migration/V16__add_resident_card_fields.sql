ALTER TABLE resident_profiles
ADD COLUMN IF NOT EXISTS national_id VARCHAR(50);

ALTER TABLE resident_profiles
ADD COLUMN IF NOT EXISTS monthly_fee DECIMAL(12,2);

CREATE UNIQUE INDEX IF NOT EXISTS uq_resident_profiles_national_id
ON resident_profiles (national_id)
WHERE national_id IS NOT NULL;
