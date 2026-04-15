CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    resident_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('MONTHLY_FEE', 'MAINTENANCE_CHARGE', 'OTHER')),
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'EGP',
    description TEXT NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoices_resident_id ON invoices(resident_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    resident_id UUID NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('CASH', 'CREDIT_CARD', 'BANK_TRANSFER', 'ONLINE_GATEWAY')),
    transaction_reference VARCHAR(255),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
