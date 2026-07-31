CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE search.search_documents ADD COLUMN embedding vector(384);


CREATE INDEX idx_search_documents_embedding
    ON search.search_documents
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
