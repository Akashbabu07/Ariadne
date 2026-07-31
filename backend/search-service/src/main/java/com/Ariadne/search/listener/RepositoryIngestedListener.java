package com.Ariadne.search.listener;

import com.Ariadne.search.client.EmbeddingClient;
import com.Ariadne.search.entity.SearchDocument;
import com.Ariadne.search.repository.SearchDocumentRepository;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RepositoryIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIngestedListener.class);

    private final SearchDocumentRepository documentRepository;
    private final EmbeddingClient embeddingClient;

    @KafkaListener(topics = "repository.ingested", groupId = "search-service")
    @Transactional
    public void onRepositoryIngested(RepositoryIngestedEvent event) {
        if (documentRepository.existsByRepositoryId(event.repositoryId())) {
            log.info("Repository {} already indexed, skipping", event.repositoryId());
            return;
        }

        SearchDocument doc = SearchDocument.builder()
                .repositoryId(event.repositoryId())
                .gitUrl(event.gitUrl())
                .build();
        doc = documentRepository.save(doc);

        log.info("Indexed repository {} for keyword search", event.repositoryId());

        try {
            List<Float> vector = embeddingClient.embed(event.gitUrl());
            documentRepository.updateEmbedding(doc.getId(), toVectorLiteral(vector));
            log.info("Generated embedding for repository {}", event.repositoryId());
        } catch (Exception e) {
            log.warn("Failed to generate embedding for repository {}: {}", event.repositoryId(), e.getMessage());
        }
    }

    private String toVectorLiteral(List<Float> vector) {
        return "[" + vector.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }
}
