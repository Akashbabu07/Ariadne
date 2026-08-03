package com.Ariadne.aigateway.client;

import com.Ariadne.grpc.parser.ParseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class SearchServiceClient {

    private final RestClient restClient = RestClient.create();

    @Value("${services.search-service.url}")
    private String searchServiceUrl;

    public void pushParsedFiles(String repositoryId, String gitUrl, ParseResponse parseResult) {
        List<Map<String, Object>> files = parseResult.getFilesList().stream()
                .map(f -> Map.<String, Object>of(
                        "path", f.getPath(),
                        "language", f.getLanguage(),
                        "content", f.getContent()
                ))
                .toList();

        String uri = UriComponentsBuilder.fromUriString(searchServiceUrl + "/api/v1/search/repositories/{id}/ingest-parsed")
                .queryParam("gitUrl", gitUrl)
                .buildAndExpand(repositoryId)
                .toUriString();

        restClient.post()
                .uri(uri)
                .body(Map.of("files", files))
                .retrieve()
                .toBodilessEntity();
    }
}