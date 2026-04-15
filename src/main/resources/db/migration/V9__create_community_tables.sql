-- V9: Create Community Tables
-- Tables for Alerts, Feedback, and Announcements

CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('MISSING_PET', 'MISSING_PERSON', 'MISSING_ITEM', 'SUSPICIOUS_ACTIVITY', 'OTHER')),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    photo_urls TEXT[],
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('COMPLAINT', 'SUGGESTION')),
    is_public BOOLEAN NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'ADDRESSED', 'CLOSED')),
    upvotes INT DEFAULT 0,
    downvotes INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feedback_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feedback_id UUID NOT NULL,
    voter_id UUID NOT NULL,
    vote_type VARCHAR(10) NOT NULL CHECK (vote_type IN ('UP', 'DOWN')),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_feedback_voter UNIQUE (feedback_id, voter_id)
);

CREATE TABLE announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    priority VARCHAR(50) NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'URGENT')),
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
