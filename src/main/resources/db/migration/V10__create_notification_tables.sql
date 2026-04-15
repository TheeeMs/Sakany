CREATE TABLE device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL CHECK (platform IN ('ANDROID', 'IOS', 'WEB')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_tokens_user_active ON device_tokens(user_id, is_active);

CREATE TABLE notification_logs (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('MAINTENANCE_UPDATE', 'PAYMENT_DUE', 'EVENT_REMINDER', 'ANNOUNCEMENT', 'ALERT', 'GENERAL')),
    reference_id UUID,
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('PUSH', 'IN_APP', 'EMAIL')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ')),
    sent_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_logs_recipient_status ON notification_logs(recipient_id, status);
