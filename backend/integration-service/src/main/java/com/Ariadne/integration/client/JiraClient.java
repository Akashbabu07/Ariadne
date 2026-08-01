package com.Ariadne.integration.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.nio.charset.StandardCharsets;

@Component
public class JiraClient {

    private final RestClient restClient = RestClient.create();

    @Value("${integration.jira.base-url:}")
    private String baseUrl;
    @Value("${integration.jira.email:}")
    private String email;
    @Value("${integration.jira.api-token:}")
    private String apiToken;
    @Value("${integration.jira.project-key:}")
    private String projectKey;

    public void createIssue(String summary, String description) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return;
        }

        String auth = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> body = Map.of(
                "fields", Map.of(
                        "project", Map.of("key", projectKey),
                        "summary", summary,
                        "description", Map.of(
                                "type", "doc", "version", 1,
                                "content", java.util.List.of(Map.of(
                                        "type", "paragraph",
                                        "content", java.util.List.of(Map.of("type", "text", "text", description))
                                ))
                        ),
                        "issuetype", Map.of("name", "Task")
                )
        );

        restClient.post()
                .uri(baseUrl + "/rest/api/3/issue")
                .header("Authorization", "Basic " + auth)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}