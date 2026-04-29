-- V31: Update seeded resident phone to requested test number.
-- Handles databases where V30 was already applied.

UPDATE users
SET phone = '+201555100100'
WHERE id = '44444444-4444-4444-4444-444444444444'
  AND NOT EXISTS (
      SELECT 1
      FROM users u2
      WHERE u2.phone = '+201555100100'
        AND u2.id <> '44444444-4444-4444-4444-444444444444'
  );
