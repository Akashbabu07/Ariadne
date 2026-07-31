package com.Ariadne.aigateway.listener;

import com.Ariadne.aigateway.client.GraphServiceClient;
import com.Ariadne.aigateway.client.ParserClient;
import com.Ariadne.grpc.parser.ParseResponse;
import com.Ariadne.shared.events.DependenciesExtractedEvent;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import com.Ariadne.shared.events.RepositoryParsedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RepositoryIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIngestedListener.class);
    private static final String PARSED_TOPIC = "repository.parsed";
    private static final String DEPENDENCIES_TOPIC = "repository.dependencies-extracted";

    private final ParserClient parserClient;
    private final GraphServiceClient graphServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "repository.ingested", groupId = "ai-gateway")
    public void onRepositoryIngested(RepositoryIngestedEvent event) {
        UUID repositoryId = event.repositoryId();
        try {
            ParseResponse parseResult = parserClient.parseRepository(repositoryId.toString(), event.gitUrl());
            publishParsed(repositoryId, event.gitUrl(), parseResult.getFilesCount());

            int edgeCount = graphServiceClient.pushParsedFiles(repositoryId.toString(), event.gitUrl(), parseResult);
            publishDependenciesExtracted(repositoryId, edgeCount);

            log.info("Auto-parsed and pushed graph for repository {} ({} files)",
                    repositoryId, parseResult.getFilesCount());
        } catch (Exception e) {
            log.warn("Auto-parse failed for repository {}: {}", repositoryId, e.getMessage());
        }
    }

    private void publishParsed(UUID repositoryId, String gitUrl, int filesParsed) {
        kafkaTemplate.send(PARSED_TOPIC, repositoryId.toString(),
                new RepositoryParsedEvent(repositoryId, gitUrl, filesParsed, Instant.now()));
    }

    private void publishDependenciesExtracted(UUID repositoryId, int edgeCount) {
        kafkaTemplate.send(DEPENDENCIES_TOPIC, repositoryId.toString(),
                new DependenciesExtractedEvent(repositoryId, edgeCount, Instant.now()));
    }
}