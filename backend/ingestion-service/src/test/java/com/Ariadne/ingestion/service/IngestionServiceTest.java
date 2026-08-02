package com.Ariadne.ingestion.service;

import com.Ariadne.ingestion.IngestionService;
import com.Ariadne.ingestion.client.ProjectServiceClient;
import com.Ariadne.ingestion.dto.IngestionJobResponse;
import com.Ariadne.ingestion.dto.StartIngestionRequest;
import com.Ariadne.ingestion.entity.IngestionJob;
import com.Ariadne.ingestion.entity.IngestionStatus;
import com.Ariadne.ingestion.repository.IngestionJobRepository;
import com.Ariadne.shared.events.RepositoryIngestedEvent;
import com.Ariadne.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock private IngestionJobRepository jobRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock private ProjectServiceClient projectServiceClient;

    private IngestionService ingestionService;
    private final UUID repositoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ingestionService = new IngestionService(jobRepository, kafkaTemplate, projectServiceClient);
    }

    @Test
    void startIngestion_createsQueuedJobAndPublishesEvent() {
        when(jobRepository.save(any(IngestionJob.class))).thenAnswer(inv -> {
            IngestionJob job = inv.getArgument(0);
            job.setId(UUID.randomUUID());
            job.setCreatedAt(Instant.now());
            job.setUpdatedAt(Instant.now());
            return job;
        });

        IngestionJobResponse response = ingestionService.startIngestion(
                new StartIngestionRequest(repositoryId, "https://github.com/x/y.git"));

        assertThat(response.status()).isEqualTo("QUEUED");
        assertThat(response.repositoryId()).isEqualTo(repositoryId);

        ArgumentCaptor<RepositoryIngestedEvent> eventCaptor = ArgumentCaptor.forClass(RepositoryIngestedEvent.class);
        verify(kafkaTemplate).send(eq("repository.ingested"), eq(repositoryId.toString()), eventCaptor.capture());
        assertThat(eventCaptor.getValue().status()).isEqualTo("QUEUED");
        assertThat(eventCaptor.getValue().gitUrl()).isEqualTo("https://github.com/x/y.git");
    }

    @Test
    void startIngestion_savedBeforeEventPublished() {

        when(jobRepository.save(any(IngestionJob.class))).thenAnswer(inv -> {
            IngestionJob job = inv.getArgument(0);
            job.setId(UUID.randomUUID());
            return job;
        });

        ingestionService.startIngestion(new StartIngestionRequest(repositoryId, "https://github.com/x/y.git"));

        InOrder inOrder = inOrder(jobRepository, kafkaTemplate);
        inOrder.verify(jobRepository).save(any(IngestionJob.class));
        inOrder.verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void startIngestion_notifiesProjectServiceOfSyncStatus() {
        when(jobRepository.save(any(IngestionJob.class))).thenAnswer(inv -> {
            IngestionJob job = inv.getArgument(0);
            job.setId(UUID.randomUUID());
            return job;
        });

        ingestionService.startIngestion(new StartIngestionRequest(repositoryId, "https://github.com/x/y.git"));

        verify(projectServiceClient).markSynced(repositoryId, "SYNCED");
    }

    @Test
    void getJob_notFound_throwsResourceNotFound() {
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingestionService.getJob(jobId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getJob_found_mapsAllFieldsCorrectly() {
        UUID jobId = UUID.randomUUID();
        IngestionJob job = IngestionJob.builder()
                .id(jobId).repositoryId(repositoryId).gitUrl("https://github.com/x/y.git")
                .status(IngestionStatus.FAILED).errorMessage("clone timed out")
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        IngestionJobResponse response = ingestionService.getJob(jobId);

        assertThat(response.status()).isEqualTo("FAILED");
        assertThat(response.errorMessage()).isEqualTo("clone timed out");
    }
}