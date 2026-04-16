-- V24: Support admin employee onboarding form fields

ALTER TABLE users
ADD COLUMN IF NOT EXISTS hire_date DATE;

ALTER TABLE users
ADD COLUMN IF NOT EXISTS department VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_users_hire_date ON users(hire_date);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department);
