ALTER TABLE feedback
DROP CONSTRAINT IF EXISTS feedback_status_check;

ALTER TABLE feedback
ADD CONSTRAINT feedback_status_check
CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'ADDRESSED', 'APPROVED', 'CLOSED'));
