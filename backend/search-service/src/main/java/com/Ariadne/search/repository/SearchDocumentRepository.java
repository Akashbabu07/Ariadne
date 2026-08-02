package com.Ariadne.search.repository;

import com.Ariadne.search.entity.SearchDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

    Optional<SearchDocument> findByRepositoryIdAndFilePath(UUID repositoryId, String filePath);

    @Query(value = """
        SELECT * FROM search.search_documents
        WHERE search_vector @@ plainto_tsquery('english', :query)
        ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
        LIMIT 20
        """, nativeQuery = true)
    List<SearchDocument> searchByKeyword(@Param("query") String query);

    @Modifying
    @Query(value = "UPDATE search.search_documents SET embedding = CAST(:vector AS vector) WHERE id = :id",
            nativeQuery = true)
    void updateEmbedding(@Param("id") UUID id, @Param("vector") String vectorLiteral);

    @Query(value = """
        SELECT * FROM search.search_documents
        WHERE embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:vector AS vector)
        LIMIT 20
        """, nativeQuery = true)
    List<SearchDocument> searchByEmbedding(@Param("vector") String queryVectorLiteral);

    @Query(value = """
        WITH keyword_ranked AS (
            SELECT id, ROW_NUMBER() OVER (
                ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
            ) AS rank
            FROM search.search_documents
            WHERE search_vector @@ plainto_tsquery('english', :query)
        ),
        vector_ranked AS (
            SELECT id, ROW_NUMBER() OVER (
                ORDER BY embedding <=> CAST(:vector AS vector)
            ) AS rank
            FROM search.search_documents
            WHERE embedding IS NOT NULL
        )
        SELECT d.* FROM search.search_documents d
        LEFT JOIN keyword_ranked k ON k.id = d.id
        LEFT JOIN vector_ranked v ON v.id = d.id
        WHERE k.id IS NOT NULL OR v.id IS NOT NULL
        ORDER BY (COALESCE(1.0 / (60 + k.rank), 0) + COALESCE(1.0 / (60 + v.rank), 0)) DESC
        LIMIT 20
        """, nativeQuery = true)
    List<SearchDocument> hybridSearch(@Param("query") String query, @Param("vector") String queryVectorLiteral);

    @Query(value = """
    WITH keyword_ranked AS (
        SELECT id, ROW_NUMBER() OVER (
            ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
        ) AS rank
        FROM search.search_documents
        WHERE repository_id = :repositoryId
          AND search_vector @@ plainto_tsquery('english', :query)
    ),
    vector_ranked AS (
        SELECT id, ROW_NUMBER() OVER (
            ORDER BY embedding <=> CAST(:vector AS vector)
        ) AS rank
        FROM search.search_documents
        WHERE repository_id = :repositoryId
          AND embedding IS NOT NULL
    )
    SELECT d.* FROM search.search_documents d
    LEFT JOIN keyword_ranked k ON k.id = d.id
    LEFT JOIN vector_ranked v ON v.id = d.id
    WHERE (k.id IS NOT NULL OR v.id IS NOT NULL) AND d.repository_id = :repositoryId
    ORDER BY (COALESCE(1.0 / (60 + k.rank), 0) + COALESCE(1.0 / (60 + v.rank), 0)) DESC
    LIMIT 10
    """, nativeQuery = true)
    List<SearchDocument> hybridSearchByRepository(@Param("query") String query,
                                                  @Param("vector") String queryVectorLiteral,
                                                  @Param("repositoryId") UUID repositoryId);
}