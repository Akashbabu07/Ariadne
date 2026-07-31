package com.Ariadne.search.repository;

import com.Ariadne.search.entity.SearchDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

    boolean existsByRepositoryId(UUID repositoryId);

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
}