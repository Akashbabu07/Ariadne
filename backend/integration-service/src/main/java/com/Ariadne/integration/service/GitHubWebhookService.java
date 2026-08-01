package com.Ariadne.integration.service;

import com.Ariadne.integration.client.IngestionServiceClient;
import com.Ariadne.integration.client.ProjectServiceClient;
import com.Ariadne.integration.dto.RepositoryStatusResponse;
import com.Ariadne.integration.dto.StartIngestionRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubWebhookService {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookService.class);

    private final ObjectMapper objectMapper;
    private final ProjectServiceClient projectServiceClient;
    private final IngestionServiceClient ingestionServiceClient;

    public void handlePush(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String cloneUrl = root.path("repository").path("clone_url").asText(null);
            if (cloneUrl == null) {
                log.warn("GitHub push payload missing repository.clone_url, ignoring");
                return;
            }

            RepositoryStatusResponse repo;
            try {
                repo = projectServiceClient.findByGitUrl(cloneUrl).data();
            } catch (Exception e) {
                log.info("Push received for unregistered repository {}, ignoring", cloneUrl);
                return;
            }

            ingestionServiceClient.startIngestion(new StartIngestionRequest(repo.id(), cloneUrl));
            log.info("Triggered re-ingestion for repository {} from GitHub push", repo.id());
        } catch (Exception e) {
            log.warn("Failed to process GitHub push webhook: {}", e.getMessage());
        }
    }
}