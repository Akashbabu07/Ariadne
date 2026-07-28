CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE knowledge.knowledge_records (
                                             id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             repository_id     UUID NOT NULL,
                                             ingestion_job_id  UUID NOT NULL UNIQUE,
                                             git_url           VARCHAR(500) NOT NULL,
                                             ingestion_status  VARCHAR(20) NOT NULL,
                                             received_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_knowledge_records_repository_id ON knowledge.knowledge_records (repository_id);