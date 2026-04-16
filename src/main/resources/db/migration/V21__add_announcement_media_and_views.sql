-- V21: Support Communications Center News & Announcements cards

ALTER TABLE announcements
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS view_count INT NOT NULL DEFAULT 0;

UPDATE announcements
SET view_count = 0
WHERE view_count < 0;
