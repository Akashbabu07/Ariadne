CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE analysis.analysis_reports (
                                           id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           repository_id  UUID NOT NULL,
                                           file_count     INTEGER NOT NULL,
                                           created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_analysis_reports_repository_id ON analysis.analysis_reports (repository_id);