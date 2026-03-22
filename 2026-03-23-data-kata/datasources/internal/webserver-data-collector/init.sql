-- Table to persist the offset managed by WebServer Data Collector
CREATE TABLE ingestion_offset (
    source_name VARCHAR(100) PRIMARY KEY,
    last_processed_id BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL
);