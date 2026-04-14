-- V1: Create Users Table
-- This is the foundation of the accounts module
-- Note: updated_at is managed by JPA @UpdateTimestamp, not database trigger

CREATE TABLE users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Identity (email nullable for phone-only users)
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    phone VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    
    -- Role (using CHECK constraint instead of ENUM for flexibility)
    role VARCHAR(20) NOT NULL 
        CHECK (role IN ('RESIDENT', 'TECHNICIAN', 'ADMIN', 'SECURITY_GUARD')),
    
    -- Auth Provider
    auth_provider VARCHAR(20) NOT NULL 
        CHECK (auth_provider IN ('PHONE_OTP', 'EMAIL_PASSWORD', 'GOOGLE')),
    
    -- State Flags
    is_phone_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps (timezone-aware, managed by JPA)
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Index for role-based queries (e.g., "get all technicians")
CREATE INDEX idx_users_role ON users(role);

-- Index for active users filter (common query pattern)
CREATE INDEX idx_users_is_active ON users(is_active);