package com.Ariadne.aigateway.client;

import com.Ariadne.aigateway.dto.RagContextResponse;
import com.Ariadne.aigateway.dto.ReasoningResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
public class ReasoningServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.reasoning-service.url}")
    private String reasoningServiceUrl;

    public ReasoningResultDto reason(UUID repositoryId, String mode, String query, RagContextResponse context) {
        List<Map<String, Object>> chunks = context.chunks().stream()
                .map(c -> Map.<String, Object>of("file_path", c.filePath(), "snippet", c.snippet()))
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("repository_id", repositoryId.toString());
        body.put("mode", mode);
        body.put("query", query);
        body.put("chunks", chunks);

        if (context.graphContext() != null) {
            var gc = context.graphContext();
            body.put("graph_context", Map.of(
                    "file_path", gc.filePath(),
                    "dependencies", gc.dependencies(),
                    "dependents", gc.dependents()
            ));
        }

        return restClient.post()
                .uri(reasoningServiceUrl + "/api/v1/reasoning/ask")
                .body(body)
                .retrieve()
                .body(ReasoningResultDto.class);
    }
}