-- V30: Seed a full resident test account with related data for QA/manual testing.
-- Idempotent inserts using fixed UUIDs.

-- Property hierarchy (compound -> building -> unit)
INSERT INTO compounds (id, name, address)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Sakany Demo Compound',
    'New Cairo - Demo Address'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO buildings (id, compound_id, name, number_of_floors)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'Building A',
    12
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO units (id, building_id, unit_number, floor, type)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '22222222-2222-2222-2222-222222222222',
    'A-1201',
    12,
    'APARTMENT'
)
ON CONFLICT (id) DO NOTHING;

-- Resident user (phone OTP login ready)
INSERT INTO users (
    id,
    email,
    password_hash,
    phone,
    first_name,
    last_name,
    role,
    auth_provider,
    is_phone_verified,
    is_active,
    employment_status
)
VALUES (
    '44444444-4444-4444-4444-444444444444',
    'resident.demo@sakany.app',
    NULL,
    '+201555100100',
    'Sakany',
    'Resident',
    'RESIDENT',
    'PHONE_OTP',
    TRUE,
    TRUE,
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;

-- Resident profile with required unit assignment and full profile fields
INSERT INTO resident_profiles (
    id,
    user_id,
    unit_id,
    move_in_date,
    resident_type,
    approval_status,
    national_id,
    monthly_fee
)
SELECT
    '55555555-5555-5555-5555-555555555555',
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    DATE '2025-01-15',
    'TENANT',
    'APPROVED',
    '29801010101010',
    1750.00
WHERE NOT EXISTS (
    SELECT 1 FROM resident_profiles
    WHERE user_id = '44444444-4444-4444-4444-444444444444'
);

-- Maintenance requests for testing multiple states
INSERT INTO maintenance_requests (
    id,
    resident_id,
    unit_id,
    title,
    description,
    location_label,
    category,
    priority,
    status,
    is_public,
    photo_urls
)
VALUES
(
    '66666666-6666-6666-6666-666666666661',
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    'Kitchen sink leakage',
    'Water leaking under kitchen sink cabinet.',
    'At Home',
    'PLUMBING',
    'NORMAL',
    'SUBMITTED',
    FALSE,
    ARRAY[]::TEXT[]
),
(
    '66666666-6666-6666-6666-666666666662',
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    'Living room power outage',
    'Power socket and lights not working in living room.',
    'At Home',
    'ELECTRICAL',
    'URGENT',
    'IN_PROGRESS',
    FALSE,
    ARRAY[]::TEXT[]
),
(
    '66666666-6666-6666-6666-666666666663',
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    'General maintenance follow-up',
    'Minor finishing work in balcony area.',
    'Neighborhood',
    'OTHER',
    'LOW',
    'RESOLVED',
    FALSE,
    ARRAY[]::TEXT[]
)
ON CONFLICT (id) DO NOTHING;

UPDATE maintenance_requests
SET resolved_at = COALESCE(resolved_at, CURRENT_TIMESTAMP),
    resolution_notes = COALESCE(resolution_notes, 'Issue fixed successfully'),
    resolution_cost = COALESCE(resolution_cost, 250.00)
WHERE id = '66666666-6666-6666-6666-666666666663';

-- Access module sample data
INSERT INTO access_codes (
    id,
    resident_id,
    visitor_name,
    visitor_phone,
    purpose,
    code,
    qr_data,
    is_single_use,
    valid_from,
    valid_until,
    status
)
VALUES (
    '77777777-7777-7777-7777-777777777777',
    '44444444-4444-4444-4444-444444444444',
    'Test Visitor',
    '+201011122233',
    'GUEST',
    'SAKANY-DEMO-001',
    'QR-SAKANY-DEMO-001',
    TRUE,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO visit_logs (
    id,
    access_code_id,
    resident_id,
    visitor_name,
    entry_time,
    gate_number
)
VALUES (
    '88888888-8888-8888-8888-888888888888',
    '77777777-7777-7777-7777-777777777777',
    '44444444-4444-4444-4444-444444444444',
    'Test Visitor',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    'G1'
)
ON CONFLICT (id) DO NOTHING;

-- Billing module sample data
INSERT INTO invoices (
    id,
    resident_id,
    unit_id,
    type,
    amount,
    currency,
    description,
    due_date,
    status,
    issued_at,
    paid_at
)
VALUES
(
    '99999999-9999-9999-9999-999999999991',
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    'MONTHLY_FEE',
    1750.00,
    'EGP',
    'April monthly maintenance fee',
    CURRENT_DATE + 5,
    'PENDING',
    CURRENT_TIMESTAMP,
    NULL
),
(
    '99999999-9999-9999-9999-999999999992',
    '44444444-4444-4444-4444-444444444444',
    '33333333-3333-3333-3333-333333333333',
    'MAINTENANCE_CHARGE',
    250.00,
    'EGP',
    'Resolved maintenance request charge',
    CURRENT_DATE - 2,
    'PAID',
    CURRENT_TIMESTAMP - INTERVAL '7 day',
    CURRENT_TIMESTAMP - INTERVAL '2 day'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (
    id,
    invoice_id,
    resident_id,
    amount,
    payment_method,
    transaction_reference,
    status
)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '99999999-9999-9999-9999-999999999992',
    '44444444-4444-4444-4444-444444444444',
    250.00,
    'ONLINE_GATEWAY',
    'TXN-SAKANY-DEMO-0001',
    'COMPLETED'
)
ON CONFLICT (id) DO NOTHING;
