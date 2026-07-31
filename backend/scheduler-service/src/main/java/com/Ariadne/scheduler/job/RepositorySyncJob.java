package com.Ariadne.scheduler.job;

import com.Ariadne.scheduler.client.IngestionServiceClient;
import com.Ariadne.scheduler.client.ProjectServiceClient;
import com.Ariadne.scheduler.dto.RepositoryResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RepositorySyncJob {

    private static final Logger log = LoggerFactory.getLogger(RepositorySyncJob.class);
    private static final Duration STALE_AFTER = Duration.ofHours(6);

    private final ProjectServiceClient projectServiceClient;
    private final IngestionServiceClient ingestionServiceClient;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void syncStaleRepositories() {
        List<RepositoryResponse> staleRepos = projectServiceClient.listStale(STALE_AFTER.toMinutes()).data();
        log.info("Found {} stale repositories", staleRepos.size());

        for (RepositoryResponse repo : staleRepos) {
            try {
                ingestionServiceClient.startIngestion(
                        new IngestionServiceClient.StartIngestionRequest(repo.id(), repo.gitUrl()));
                log.info("Triggered re-ingestion for repository {}", repo.id());
            } catch (Exception e) {

                log.warn("Failed to trigger ingestion for repository {}: {}", repo.id(), e.getMessage());
            }
        }
    }
}
