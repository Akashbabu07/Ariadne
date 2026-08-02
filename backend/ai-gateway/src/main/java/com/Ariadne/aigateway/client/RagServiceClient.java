
package com.Ariadne.aigateway.client;

import com.Ariadne.aigateway.dto.RagContextResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RagServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.rag-service.url}")
    private String ragServiceUrl;

    public RagContextResponse fetchContext(UUID repositoryId, String query, String filePath) {
        Map<String, Object> body = new HashMap<>();
        body.put("repository_id", repositoryId.toString());
        body.put("query", query);
        if (filePath != null) {
            body.put("file_path", filePath);
        }

        return restClient.post()
                .uri(ragServiceUrl + "/api/v1/rag/context")
                .body(body)
                .retrieve()
                .body(RagContextResponse.class);
    }
}