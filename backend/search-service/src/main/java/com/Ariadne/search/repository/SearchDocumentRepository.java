package com.Ariadne.search.repository;

import com.Ariadne.search.entity.SearchDocument;
import org.springframework.data.jpa.repository.JpaRepository;
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
}