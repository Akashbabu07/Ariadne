package com.Ariadne.aigateway.client;

import com.Ariadne.grpc.parser.ParseResponse;
import com.Ariadne.shared.events.GraphUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GraphServiceClient {

    private static final Logger log = LoggerFactory.getLogger(GraphServiceClient.class);

    private final RestClient restClient = RestClient.create();
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${services.graph-service.url}")
    private String graphServiceUrl;

    public GraphServiceClient(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public int pushParsedFiles(String repositoryId, String gitUrl, ParseResponse parseResult) {
        restClient.post()
                .uri(graphServiceUrl + "/api/v1/graph/repositories")
                .body(Map.of("repositoryId", repositoryId, "gitUrl", gitUrl))
                .retrieve()
                .toBodilessEntity();

        List<Map<String, Object>> files = parseResult.getFilesList().stream()
                .map(f -> Map.<String, Object>of(
                        "path", f.getPath(),
                        "language", f.getLanguage(),
                        "imports", f.getImportsList()
                ))
                .toList();

        restClient.post()
                .uri(graphServiceUrl + "/api/v1/graph/repositories/{id}/ingest-parsed", repositoryId)
                .body(Map.of("files", files))
                .retrieve()
                .toBodilessEntity();

        kafkaTemplate.send("graph.updated", repositoryId,
                new GraphUpdatedEvent(UUID.fromString(repositoryId), parseResult.getFilesCount(), Instant.now()));
        return 0;
    }
}
