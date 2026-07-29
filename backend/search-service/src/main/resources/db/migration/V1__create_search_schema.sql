CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE search.search_documents (
                                         id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         repository_id  UUID NOT NULL UNIQUE,
                                         git_url        VARCHAR(500) NOT NULL,
                                         indexed_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         search_vector  TSVECTOR GENERATED ALWAYS AS (to_tsvector('english', git_url)) STORED
);
CREATE INDEX idx_search_documents_vector ON search.search_documents USING GIN (search_vector);