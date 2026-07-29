package com.Ariadne.search.listener;

import com.Ariadne.search.entity.SearchDocument;
import com.Ariadne.search.repository.SearchDocumentRepository;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RepositoryIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIngestedListener.class);

    private final SearchDocumentRepository documentRepository;

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
        documentRepository.save(doc);

        log.info("Indexed repository {} for keyword search", event.repositoryId());
    }
}