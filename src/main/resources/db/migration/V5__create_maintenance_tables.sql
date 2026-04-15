CREATE TABLE maintenance_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resident_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    technician_id UUID,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL CHECK (category IN ('PLUMBING','ELECTRICAL','HVAC','ELEVATOR','OTHER')),
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW','NORMAL','URGENT','EMERGENCY')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CANCELLED','REJECTED')),
    is_public BOOLEAN DEFAULT FALSE,
    photo_urls TEXT[],
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_maintenance_requests_resident_id ON maintenance_requests(resident_id);
CREATE INDEX idx_maintenance_requests_technician_id ON maintenance_requests(technician_id);
CREATE INDEX idx_maintenance_requests_status ON maintenance_requests(status);
CREATE INDEX idx_maintenance_requests_unit_id ON maintenance_requests(unit_id);
