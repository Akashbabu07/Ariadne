package com.Ariadne.aigateway.listener;

import com.Ariadne.aigateway.client.GraphServiceClient;
import com.Ariadne.aigateway.client.ParserClient;
import com.Ariadne.grpc.parser.ParseResponse;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RepositoryIngestedListener {

    private static final Logger log = LoggerFactory.getLogger(RepositoryIngestedListener.class);

    private final ParserClient parserClient;
    private final GraphServiceClient graphServiceClient;

    @KafkaListener(topics = "repository.ingested", groupId = "ai-gateway")
    public void onRepositoryIngested(RepositoryIngestedEvent event) {
        String repositoryId = event.repositoryId().toString();
        try {
            ParseResponse parseResult = parserClient.parseRepository(repositoryId, event.gitUrl());
            graphServiceClient.pushParsedFiles(repositoryId, event.gitUrl(), parseResult);
            log.info("Auto-parsed and pushed graph for repository {} ({} files)",
                    repositoryId, parseResult.getFilesCount());
        } catch (Exception e) {

            log.warn("Auto-parse failed for repository {}: {}", repositoryId, e.getMessage());
        }
    }
}
