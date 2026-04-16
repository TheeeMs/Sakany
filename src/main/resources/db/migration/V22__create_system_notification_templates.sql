-- V22: System notification templates for Communications Center automation tab

CREATE TABLE IF NOT EXISTS system_notification_templates (
    id UUID PRIMARY KEY,
    template_key VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    message_template TEXT NOT NULL,
    trigger_condition VARCHAR(255) NOT NULL,
    notification_type VARCHAR(50) NOT NULL CHECK (notification_type IN ('MAINTENANCE_UPDATE', 'PAYMENT_DUE', 'EVENT_REMINDER', 'ANNOUNCEMENT', 'ALERT', 'GENERAL')),
    keywords VARCHAR(500),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_notification_templates_enabled
ON system_notification_templates(is_enabled);

CREATE INDEX IF NOT EXISTS idx_system_notification_templates_order
ON system_notification_templates(display_order);

INSERT INTO system_notification_templates (
    id,
    template_key,
    title,
    category,
    message_template,
    trigger_condition,
    notification_type,
    keywords,
    is_enabled,
    display_order
)
VALUES
    (gen_random_uuid(), 'PAYMENT_DUE_REMINDER', 'Payment Due Reminder', 'Payment', 'Your monthly payment of {amount} EGP is due on {date}. Please pay before the deadline to avoid late fees.', '3 days before due date', 'PAYMENT_DUE', 'due,payment,reminder', TRUE, 1),
    (gen_random_uuid(), 'PAYMENT_OVERDUE_NOTICE', 'Payment Overdue Notice', 'Payment', 'Your payment of {amount} EGP is now overdue. Please settle immediately to avoid service interruption.', '1 day after due date', 'PAYMENT_DUE', 'overdue,payment', TRUE, 2),
    (gen_random_uuid(), 'MAINTENANCE_REQUEST_RECEIVED', 'Maintenance Request Received', 'Maintenance', 'We received your maintenance request #{ticketId}. A technician will be assigned shortly.', 'When ticket created', 'MAINTENANCE_UPDATE', 'maintenance,received,request', TRUE, 3),
    (gen_random_uuid(), 'MAINTENANCE_COMPLETED', 'Maintenance Completed', 'Maintenance', 'Your maintenance request #{ticketId} has been completed. Please rate the service.', 'When ticket closed', 'MAINTENANCE_UPDATE', 'maintenance,completed,closed', TRUE, 4),
    (gen_random_uuid(), 'SECURITY_ALERT_PUSH', 'Security Alert Triggered', 'Security', 'Security alert: {alertTitle}. Authorities have been notified and updates will follow.', 'When security alert is reported', 'ALERT', 'security,alert', TRUE, 5),
    (gen_random_uuid(), 'EVENT_REMINDER_AUTOMATED', 'Upcoming Event Reminder', 'General', 'Reminder: {eventTitle} starts on {date}. Check details in the Events section.', '24 hours before event start', 'EVENT_REMINDER', 'event,reminder', TRUE, 6),
    (gen_random_uuid(), 'ANNOUNCEMENT_BROADCAST', 'Announcement Broadcast', 'General', 'A new community announcement is available. Open the app to read full details.', 'When announcement is published', 'ANNOUNCEMENT', 'announcement,broadcast', FALSE, 7)
ON CONFLICT (template_key) DO NOTHING;
