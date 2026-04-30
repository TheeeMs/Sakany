-- V37: Seed a few admin-managed community events (idempotent)
-- These seeds are for dashboard demo/QA and safe to re-run.

INSERT INTO community_events (
    id,
    organizer_id,
    title,
    description,
    location,
    start_date,
    end_date,
    image_url,
    host_name,
    price,
    max_attendees,
    category,
    host_role,
    contact_phone,
    latitude,
    longitude,
    tags,
    recurring_event,
    current_attendees,
    status,
    approved_by
)
VALUES
(
    'e1000000-0000-4000-8000-000000000100',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', -- admin user
    'Admin Welcome Gathering',
    'A short welcome gathering organized by the admin team for new residents.',
    'Community Hall',
    CURRENT_TIMESTAMP + INTERVAL '7 day',
    CURRENT_TIMESTAMP + INTERVAL '7 day 2 hour',
    NULL,
    'Sakany Admin',
    0,
    120,
    'COMMUNITY',
    'Administration',
    '+201555900001',
    30.0100,
    31.2100,
    'welcome,new,residents',
    FALSE,
    0,
    'APPROVED',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
),
(
    'e1000000-0000-4000-8000-000000000101',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'Community Safety Workshop',
    'Workshop about safety procedures, emergency contacts and community rules.',
    'Conference Room B',
    CURRENT_TIMESTAMP + INTERVAL '14 day',
    CURRENT_TIMESTAMP + INTERVAL '14 day 3 hour',
    NULL,
    'Sakany Admin',
    0,
    60,
    'EDUCATION',
    'Administration',
    '+201555900001',
    30.0150,
    31.2150,
    'safety,workshop,admin',
    FALSE,
    0,
    'PROPOSED',
    NULL
),
(
    'e1000000-0000-4000-8000-000000000102',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'Pool Rules Reminder',
    'Short session for pool users covering rules and schedule.',
    'Swimming Pool',
    CURRENT_TIMESTAMP - INTERVAL '10 day',
    CURRENT_TIMESTAMP - INTERVAL '10 day' + INTERVAL '1 hour',
    NULL,
    'Sakany Admin',
    0,
    40,
    'SPORTS',
    'Administration',
    '+201555900001',
    30.0200,
    31.2200,
    'pool,reminder,regulations',
    FALSE,
    28,
    'COMPLETED',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
)
ON CONFLICT (id) DO NOTHING;

-- Optional registrations for testing export
INSERT INTO event_registrations (
    id,
    event_id,
    resident_id,
    registered_at,
    status
)
VALUES
(
    'f1000000-0000-4000-8000-000000000100',
    'e1000000-0000-4000-8000-000000000100',
    '44444444-4444-4444-4444-444444444444',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    'REGISTERED'
),
(
    'f1000000-0000-4000-8000-000000000101',
    'e1000000-0000-4000-8000-000000000102',
    '44444444-4444-4444-4444-444444444444',
    CURRENT_TIMESTAMP - INTERVAL '12 day',
    'REGISTERED'
)
ON CONFLICT (id) DO NOTHING;
