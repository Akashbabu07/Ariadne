ALTER TABLE search.search_documents DROP COLUMN search_vector;
ALTER TABLE search.search_documents DROP CONSTRAINT search_documents_repository_id_key;

ALTER TABLE search.search_documents
    ADD COLUMN file_path VARCHAR(1000),
    ADD COLUMN content TEXT;

ALTER TABLE search.search_documents
    ADD COLUMN search_vector TSVECTOR GENERATED ALWAYS AS (to_tsvector('english', coalesce(content, ''))) STORED;

ALTER TABLE search.search_documents
    ADD CONSTRAINT uq_search_documents_repo_file UNIQUE (repository_id, file_path);

DROP INDEX search.idx_search_documents_vector;
CREATE INDEX idx_search_documents_vector ON search.search_documents USING GIN (search_vector);