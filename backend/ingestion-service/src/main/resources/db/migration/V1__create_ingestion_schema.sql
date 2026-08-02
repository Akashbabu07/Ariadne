CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE ingestion.ingestion_jobs (
                                          id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          repository_id  UUID NOT NULL,
                                          git_url        VARCHAR(500) NOT NULL,
                                          status         VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
                                          error_message  VARCHAR(2000),
                                          created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ingestion_jobs_repository_id ON ingestion.ingestion_jobs (repository_id);