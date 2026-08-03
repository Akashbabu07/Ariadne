package com.Ariadne.service;



import com.Ariadne.analysis.AnalysisService;
import com.Ariadne.analysis.client.GraphServiceClient;
import com.Ariadne.analysis.client.ProjectServiceClient;
import com.Ariadne.analysis.client.ReasoningServiceClient;
import com.Ariadne.analysis.dto.*;
import com.Ariadne.analysis.entity.*;
import com.Ariadne.analysis.repository.*;
import com.Ariadne.analysis.dto.*;
import com.Ariadne.analysis.entity.DriftReport;
import com.Ariadne.analysis.entity.FileMetricSnapshot;
import com.Ariadne.analysis.entity.ImpactAnalysisReport;
import com.Ariadne.analysis.repository.AnalysisReportRepository;
import com.Ariadne.analysis.repository.DriftReportRepository;
import com.Ariadne.analysis.repository.FileMetricSnapshotRepository;
import com.Ariadne.analysis.repository.ImpactAnalysisReportRepository;
import com.Ariadne.shared.events.DriftDetectedEvent;
import com.Ariadne.shared.response.ApiEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalysisServiceTest {


    @Mock private GraphServiceClient graphServiceClient;
    @Mock private AnalysisReportRepository reportRepository;
    @Mock private ImpactAnalysisReportRepository impactAnalysisReportRepository;
    @Mock private DriftReportRepository driftReportRepository;
    @Mock private FileMetricSnapshotRepository fileMetricSnapshotRepository;
    @Mock private ReasoningServiceClient reasoningServiceClient;
    @Mock private ProjectServiceClient projectServiceClient;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    private AnalysisService analysisService;
    private final UUID repositoryId = UUID.randomUUID();

    @BeforeEach
    void setUp(){

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        analysisService = new AnalysisService(
                graphServiceClient, reportRepository, kafkaTemplate,
                impactAnalysisReportRepository, driftReportRepository,
                fileMetricSnapshotRepository, reasoningServiceClient,
                objectMapper, projectServiceClient
        );
    }

    @Test
    void runImpactAnalysis_persistsImpactedFilesAndExplanation() {
        String targetPath = "src/main/AuthService.java";
        List<RepositoryGraphResponse.FileResponse> impacted = List.of(
                new RepositoryGraphResponse.FileResponse(1L, "src/main/LoginController.java", "java"),
                new RepositoryGraphResponse.FileResponse(2L, "src/main/TokenValidator.java", "java")
        );

        when(graphServiceClient.getTransitiveDependents(repositoryId.toString(), targetPath))
                .thenReturn(new ApiEnvelope<>(true, impacted));
        when(reasoningServiceClient.reason(eq(repositoryId), eq("impact_analysis"), anyString(), eq(targetPath), eq(List.of()), anyList()))
                .thenReturn(new ReasoningServiceClient.ReasoningResult(repositoryId, "impact_analysis", "Two controllers depend on this.", List.of()));
        when(impactAnalysisReportRepository.save(any(ImpactAnalysisReport.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ImpactAnalysisReportResponse response = analysisService.runImpactAnalysis(repositoryId, targetPath);

        assertThat(response.impactedFiles()).containsExactly(
                "src/main/LoginController.java", "src/main/TokenValidator.java");
        assertThat(response.explanation()).isEqualTo("Two controllers depend on this.");

        ArgumentCaptor<ImpactAnalysisReport> captor = ArgumentCaptor.forClass(ImpactAnalysisReport.class);
        verify(impactAnalysisReportRepository).save(captor.capture());
        assertThat(captor.getValue().getImpactedFilesJson())
                .contains("LoginController.java", "TokenValidator.java");
    }

    @Test
    void runDriftDetection_firstRun_establishesBaselineWithoutFlaggingDrift() {

        when(graphServiceClient.getFileMetrics(repositoryId.toString())).thenReturn(
                new ApiEnvelope<>(true, List.of(new FileMetricsResponse("src/a.java", "java", 3, 5))));
        when(fileMetricSnapshotRepository.findLatestBatch(repositoryId)).thenReturn(List.of());
        when(driftReportRepository.save(any(DriftReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DriftReportResponse response = analysisService.runDriftDetection(repositoryId);

        assertThat(response.driftedFiles()).isEmpty();
        assertThat(response.explanation()).isNull();
        verify(reasoningServiceClient, never()).reason(any(), any(), any(), any(), any(), any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(fileMetricSnapshotRepository).saveAll(anyList());
    }

    @Test
    void runDriftDetection_significantCouplingChange_flagsAndPublishesEvent() {
        FileMetricSnapshot previous = FileMetricSnapshot.builder()
                .repositoryId(repositoryId).filePath("src/a.java").fanIn(2).fanOut(1).build();
        when(fileMetricSnapshotRepository.findLatestBatch(repositoryId)).thenReturn(List.of(previous));
        when(graphServiceClient.getFileMetrics(repositoryId.toString())).thenReturn(
                new ApiEnvelope<>(true, List.of(new FileMetricsResponse("src/a.java", "java", 6, 1))));
        when(reasoningServiceClient.reason(eq(repositoryId), eq("drift_explanation"), anyString(), eq(null), eq(List.of()), eq(List.of())))
                .thenReturn(new ReasoningServiceClient.ReasoningResult(repositoryId, "drift_explanation", "Fan-in grew sharply.", List.of()));
        when(driftReportRepository.save(any(DriftReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DriftReportResponse response = analysisService.runDriftDetection(repositoryId);

        assertThat(response.driftedFiles()).hasSize(1);
        assertThat(response.driftedFiles().get(0).prevFanIn()).isEqualTo(2);
        assertThat(response.driftedFiles().get(0).newFanIn()).isEqualTo(6);
        assertThat(response.explanation()).isEqualTo("Fan-in grew sharply.");
        verify(kafkaTemplate).send(eq("repository.drift-detected"), eq(repositoryId.toString()), any(DriftDetectedEvent.class));
    }

    @Test
    void runDriftDetection_smallChangeBelowThreshold_notFlagged() {
        FileMetricSnapshot previous = FileMetricSnapshot.builder()
                .repositoryId(repositoryId).filePath("src/a.java").fanIn(2).fanOut(1).build();
        when(fileMetricSnapshotRepository.findLatestBatch(repositoryId)).thenReturn(List.of(previous));
        when(graphServiceClient.getFileMetrics(repositoryId.toString())).thenReturn(
                new ApiEnvelope<>(true, List.of(new FileMetricsResponse("src/a.java", "java", 3, 2))));
        when(driftReportRepository.save(any(DriftReport.class))).thenAnswer(inv -> inv.getArgument(0));

        DriftReportResponse response = analysisService.runDriftDetection(repositoryId);

        assertThat(response.driftedFiles()).isEmpty();
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
    @Test
    void getRepositoryHealth_brandNewRepoWithNoReportsYet_degradesGracefully() {
        when(projectServiceClient.getRepository(repositoryId.toString())).thenReturn(
                new ApiEnvelope<>(true, new RepositoryStatusResponse(
                        repositoryId, UUID.randomUUID(), "https://github.com/x/y.git", "main",
                        "PENDING", null, Instant.now())));
        when(reportRepository.findByRepositoryId(repositoryId)).thenReturn(List.of());
        when(graphServiceClient.getFileMetrics(repositoryId.toString())).thenReturn(new ApiEnvelope<>(true, List.of()));
        when(driftReportRepository.findByRepositoryId(repositoryId)).thenReturn(List.of());

        RepositoryHealthResponse health = analysisService.getRepositoryHealth(repositoryId);

        assertThat(health.fileCount()).isZero();
        assertThat(health.dependencyEdgeCount()).isZero();
        assertThat(health.lastAnalysisAt()).isNull();
        assertThat(health.driftDetected()).isFalse();
        assertThat(health.lastDriftCheckAt()).isNull();
    }
}
