DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE rel.relname = 'feedback'
          AND nsp.nspname = current_schema()
          AND con.contype = 'c'
          AND pg_get_constraintdef(con.oid) ILIKE '%status%'
    LOOP
        EXECUTE format('ALTER TABLE feedback DROP CONSTRAINT IF EXISTS %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE feedback DROP CONSTRAINT IF EXISTS chk_feedback_status;

ALTER TABLE feedback
    ADD CONSTRAINT chk_feedback_status
    CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'ADDRESSED', 'APPROVED', 'CLOSED'));
