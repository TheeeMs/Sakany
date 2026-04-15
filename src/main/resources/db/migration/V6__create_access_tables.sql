-- V6: Create Access Module Tables (Visitor & QR Code Management)

CREATE TABLE access_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resident_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    visitor_name VARCHAR(255) NOT NULL,
    visitor_phone VARCHAR(20),
    purpose VARCHAR(50) NOT NULL, -- GUEST, DELIVERY, SERVICE, OTHER
    code VARCHAR(8) NOT NULL UNIQUE,
    qr_data TEXT NOT NULL,
    is_single_use BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL, -- ACTIVE, USED, EXPIRED, REVOKED
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE visit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    access_code_id UUID NOT NULL REFERENCES access_codes(id) ON DELETE CASCADE,
    resident_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    visitor_name VARCHAR(255) NOT NULL,
    entry_time TIMESTAMPTZ NOT NULL,
    exit_time TIMESTAMPTZ,
    gate_number VARCHAR(10),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_access_codes_resident_id ON access_codes(resident_id);
CREATE INDEX idx_access_codes_status ON access_codes(status);
CREATE INDEX idx_access_codes_code ON access_codes(code);
CREATE INDEX idx_access_codes_valid_until ON access_codes(valid_until);

CREATE INDEX idx_visit_logs_resident_id ON visit_logs(resident_id);
CREATE INDEX idx_visit_logs_access_code_id ON visit_logs(access_code_id);
CREATE INDEX idx_visit_logs_entry_time ON visit_logs(entry_time);
