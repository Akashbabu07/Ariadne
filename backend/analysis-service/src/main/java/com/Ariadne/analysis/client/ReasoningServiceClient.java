package com.Ariadne.analysis.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class ReasoningServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.reasoning-service.url}")
    private String reasoningServiceUrl;

    public ReasoningResult reason(UUID repositoryId, String mode, String query,
                                  String targetFilePath, List<String> dependencies, List<String> dependents) {
        Map<String, Object> body = new HashMap<>();
        body.put("repository_id", repositoryId.toString());
        body.put("mode", mode);
        body.put("query", query);
        body.put("chunks", List.of());

        if (targetFilePath != null) {
            body.put("graph_context", Map.of(
                    "file_path", targetFilePath,
                    "dependencies", dependencies,
                    "dependents", dependents
            ));
        }

        return restClient.post()
                .uri(reasoningServiceUrl + "/api/v1/reasoning/ask")
                .body(body)
                .retrieve()
                .body(ReasoningResult.class);
    }

    public record ReasoningResult(
            @JsonProperty("repository_id") UUID repositoryId,
            String mode, String answer, List<String> sources
    ) {}
}