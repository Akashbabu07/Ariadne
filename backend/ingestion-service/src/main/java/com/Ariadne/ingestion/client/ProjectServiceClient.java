package com.Ariadne.ingestion.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;


@Component
public class ProjectServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceClient.class);

    private final RestClient restClient = RestClient.create();

    @Value("${services.project-service.url}")
    private String projectServiceUrl;

    public void markSynced(UUID repositoryId, String status) {
        try {
            restClient.patch()
                    .uri(projectServiceUrl + "/api/v1/repositories/{id}/sync-status?status={status}",
                            repositoryId, status)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {

            log.warn("Failed to update sync status for repository {}: {}", repositoryId, e.getMessage());
        }
    }
}
