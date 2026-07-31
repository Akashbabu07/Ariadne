package com.Ariadne.ingestion.service;

import com.Ariadne.ingestion.client.ProjectServiceClient;
import com.Ariadne.ingestion.dto.*;
import com.Ariadne.ingestion.entity.*;
import com.Ariadne.ingestion.repository.IngestionJobRepository;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private static final String TOPIC = "repository.ingested";

    private final IngestionJobRepository jobRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProjectServiceClient projectServiceClient;

    @Transactional
    public IngestionJobResponse startIngestion(StartIngestionRequest request) {
        IngestionJob job = IngestionJob.builder()
                .repositoryId(request.repositoryId())
                .gitUrl(request.gitUrl())
                .status(IngestionStatus.QUEUED)
                .build();
        job = jobRepository.save(job);
        publishEvent(job);

        projectServiceClient.markSynced(job.getRepositoryId(), "SYNCED");

        return toResponse(job);
    }

    public IngestionJobResponse getJob(UUID jobId) {
        return toResponse(jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingestion job not found")));
    }

    private void publishEvent(IngestionJob job) {
        RepositoryIngestedEvent event = new RepositoryIngestedEvent(
                job.getId(), job.getRepositoryId(), job.getGitUrl(),
                job.getStatus().name(), Instant.now()
        );
        kafkaTemplate.send(TOPIC, job.getRepositoryId().toString(), event);
    }

    private IngestionJobResponse toResponse(IngestionJob job) {
        return new IngestionJobResponse(
                job.getId(), job.getRepositoryId(), job.getGitUrl(),
                job.getStatus().name(), job.getErrorMessage(),
                job.getCreatedAt(), job.getUpdatedAt()
        );
    }
}