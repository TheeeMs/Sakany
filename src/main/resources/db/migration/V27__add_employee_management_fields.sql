-- V27: Add Employee Management fields to users
-- Adds hire_date, department, and explicit employment_status for admin employee dashboard compatibility.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS hire_date DATE;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS department VARCHAR(100);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS employment_status VARCHAR(20);

UPDATE users
SET employment_status = CASE WHEN is_active THEN 'ACTIVE' ELSE 'INACTIVE' END
WHERE employment_status IS NULL OR TRIM(employment_status) = '';

ALTER TABLE users
    ALTER COLUMN employment_status SET DEFAULT 'ACTIVE';

ALTER TABLE users
    ALTER COLUMN employment_status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_users_employment_status'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT chk_users_employment_status
                CHECK (employment_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_users_employment_status ON users(employment_status);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department);
CREATE INDEX IF NOT EXISTS idx_users_hire_date ON users(hire_date);
