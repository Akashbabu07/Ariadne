
CREATE TABLE analysis.impact_analysis_reports (
                                                  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  repository_id  UUID NOT NULL,
                                                  target_path    VARCHAR(1000) NOT NULL,
                                                  impacted_files TEXT NOT NULL,   -- JSON array, serialized in the service layer
                                                  explanation    TEXT,
                                                  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_impact_reports_repository_id ON analysis.impact_analysis_reports (repository_id);

CREATE TABLE analysis.file_metric_snapshots (
                                                id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                repository_id UUID NOT NULL,
                                                file_path     VARCHAR(1000) NOT NULL,
                                                fan_in        INTEGER NOT NULL,
                                                fan_out       INTEGER NOT NULL,
                                                captured_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_file_metric_snapshots_repo_captured ON analysis.file_metric_snapshots (repository_id, captured_at);

CREATE TABLE analysis.drift_reports (
                                        id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        repository_id  UUID NOT NULL,
                                        drifted_files  TEXT NOT NULL,   -- JSON array, serialized in the service layer
                                        explanation    TEXT,
                                        created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_drift_reports_repository_id ON analysis.drift_reports (repository_id);