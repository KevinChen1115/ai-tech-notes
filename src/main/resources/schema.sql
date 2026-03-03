CREATE TABLE IF NOT EXISTS raw_posts(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    platform VARCHAR(50),
    author VARCHAR(255),
    content TEXT,
    url VARCHAR(500),
    scraped_at TIMESTAMP,
    is_processed BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_is_processed ON raw_posts(is_processed);

CREATE TABLE IF NOT EXISTS ai_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID REFERENCES raw_posts(id),
    is_valuable BOOLEAN,
    tags VARCHAR(500),
    summary TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);