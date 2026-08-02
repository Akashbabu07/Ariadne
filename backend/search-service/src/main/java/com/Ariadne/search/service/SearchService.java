package com.Ariadne.search.service;

import com.Ariadne.search.client.EmbeddingClient;
import com.Ariadne.search.dto.IngestParsedFilesRequest;
import com.Ariadne.search.dto.SearchResultResponse;
import com.Ariadne.search.entity.SearchDocument;
import com.Ariadne.search.repository.SearchDocumentRepository;
import com.Ariadne.shared.events.EmbeddingsGeneratedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final SearchDocumentRepository documentRepository;
    private final EmbeddingClient embeddingClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public List<SearchResultResponse> keywordSearch(String query) {
        return documentRepository.searchByKeyword(query).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SearchResultResponse> semanticSearch(String query) {
        List<Float> queryVector = embeddingClient.embed(query);
        return documentRepository.searchByEmbedding(toVectorLiteral(queryVector)).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SearchResultResponse> hybridSearch(String query) {
        List<Float> vector = embeddingClient.embed(query);
        return documentRepository.hybridSearch(query, toVectorLiteral(vector)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void indexParsedFiles(UUID repositoryId, String gitUrl, List<IngestParsedFilesRequest.ParsedFileDto> files) {
        int embedded = 0;
        for (var f : files) {
            SearchDocument doc = documentRepository
                    .findByRepositoryIdAndFilePath(repositoryId, f.path())
                    .orElseGet(() -> SearchDocument.builder()
                            .repositoryId(repositoryId)
                            .gitUrl(gitUrl)
                            .filePath(f.path())
                            .build());
            doc.setContent(f.content());
            doc = documentRepository.save(doc);

            try {
                List<Float> vector = embeddingClient.embed(f.content());
                documentRepository.updateEmbedding(doc.getId(), toVectorLiteral(vector));
                embedded++;
            } catch (Exception e) {
                log.warn("Embedding failed for {}#{}: {}", repositoryId, f.path(), e.getMessage());
            }
        }

        if (embedded > 0) {
            kafkaTemplate.send("repository.embeddings-generated", repositoryId.toString(),
                    new EmbeddingsGeneratedEvent(repositoryId, 384, Instant.now()));
        }
        log.info("Indexed {} files ({} embedded) for repository {}", files.size(), embedded, repositoryId);
    }

    private String toVectorLiteral(List<Float> vector) {
        return "[" + vector.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }

    private SearchResultResponse toResponse(SearchDocument d) {
        String snippet = d.getContent() == null ? null
                : d.getContent().substring(0, Math.min(200, d.getContent().length()));
        return new SearchResultResponse(d.getRepositoryId(), d.getGitUrl(), d.getFilePath(), snippet, d.getIndexedAt());
    }

    public List<SearchResultResponse> hybridSearch(String query, UUID repositoryId) {
        List<Float> vector = embeddingClient.embed(query);
        List<SearchDocument> results = (repositoryId != null)
                ? documentRepository.hybridSearchByRepository(query, toVectorLiteral(vector), repositoryId)
                : documentRepository.hybridSearch(query, toVectorLiteral(vector));
        return results.stream().map(this::toResponse).toList();
    }
}