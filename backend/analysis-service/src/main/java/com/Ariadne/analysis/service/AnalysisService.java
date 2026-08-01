package com.Ariadne.analysis.service;

import com.Ariadne.analysis.client.GraphServiceClient;
import com.Ariadne.analysis.client.ReasoningServiceClient;
import com.Ariadne.analysis.dto.*;
import com.Ariadne.analysis.entity.AnalysisReport;
import com.Ariadne.analysis.entity.DriftReport;
import com.Ariadne.analysis.entity.FileMetricSnapshot;
import com.Ariadne.analysis.entity.ImpactAnalysisReport;
import com.Ariadne.analysis.repository.AnalysisReportRepository;
import com.Ariadne.analysis.repository.DriftReportRepository;
import com.Ariadne.analysis.repository.FileMetricSnapshotRepository;
import com.Ariadne.analysis.repository.ImpactAnalysisReportRepository;
import com.Ariadne.shared.events.AnalysisCompletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final String TOPIC = "analysis.completed";

    private final GraphServiceClient graphServiceClient;
    private final AnalysisReportRepository reportRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ImpactAnalysisReportRepository impactAnalysisReportRepository;
    private final DriftReportRepository driftReportRepository;
    private final FileMetricSnapshotRepository fileMetricSnapshotRepository;
    private final ReasoningServiceClient reasoningServiceClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisReportResponse runBasicAnalysis(UUID repositoryId) {
        RepositoryGraphResponse graph = graphServiceClient.getRepositoryGraph(repositoryId.toString()).data();

        AnalysisReport report = AnalysisReport.builder()
                .repositoryId(repositoryId)
                .fileCount(graph.files().size())
                .build();
        report = reportRepository.save(report);

        kafkaTemplate.send(TOPIC, repositoryId.toString(),
                new AnalysisCompletedEvent(repositoryId, report.getId(), Instant.now()));

        return toResponse(report);
    }

    private AnalysisReportResponse toResponse(AnalysisReport r) {
        return new AnalysisReportResponse(r.getId(), r.getRepositoryId(), r.getFileCount(), r.getCreatedAt());
    }

    @Transactional
    public ImpactAnalysisReportResponse runImpactAnalysis(UUID repositoryId, String targetPath) {
        List<String> impactedPaths = graphServiceClient
                .getTransitiveDependents(repositoryId.toString(), targetPath).data().stream()
                .map(RepositoryGraphResponse.FileResponse::path)
                .toList();

        String query = "If %s changes, what needs review?".formatted(targetPath);
        ReasoningServiceClient.ReasoningResult result = reasoningServiceClient.reason(
                repositoryId, "impact_analysis", query, targetPath, List.of(), impactedPaths);

        ImpactAnalysisReport report = ImpactAnalysisReport.builder()
                .repositoryId(repositoryId)
                .targetPath(targetPath)
                .impactedFilesJson(toJson(impactedPaths))
                .explanation(result.answer())
                .build();
        report = impactAnalysisReportRepository.save(report);

        return new ImpactAnalysisReportResponse(
                report.getId(), repositoryId, targetPath, impactedPaths, report.getExplanation(), report.getCreatedAt());
    }
    @Transactional
    public DriftReportResponse runDriftDetection(UUID repositoryId) {
        List<FileMetricsResponse> current = graphServiceClient.getFileMetrics(repositoryId.toString()).data();
        Map<String, FileMetricSnapshot> previousByPath = fileMetricSnapshotRepository
                .findLatestBatch(repositoryId).stream()
                .collect(Collectors.toMap(FileMetricSnapshot::getFilePath, f -> f));

        List<DriftedFile> drifted = new ArrayList<>();
        for (FileMetricsResponse f : current) {
            FileMetricSnapshot prev = previousByPath.get(f.path());
            if (prev == null) continue; // new file since last run — nothing to diff against yet

            int fanInDelta = (int) f.fanIn() - prev.getFanIn();
            int fanOutDelta = (int) f.fanOut() - prev.getFanOut();

            if (Math.abs(fanInDelta) >= 2 || Math.abs(fanOutDelta) >= 2) {
                drifted.add(new DriftedFile(f.path(), prev.getFanIn(), (int) f.fanIn(), prev.getFanOut(), (int) f.fanOut()));
            }
        }

        fileMetricSnapshotRepository.saveAll(current.stream()
                .map(f -> FileMetricSnapshot.builder()
                        .repositoryId(repositoryId).filePath(f.path())
                        .fanIn((int) f.fanIn()).fanOut((int) f.fanOut()).build())
                .toList());

        String explanation = null;
        if (!drifted.isEmpty()) {
            String query = "Coupling changed for %d file(s) since the last analysis run: %s. Explain the risk."
                    .formatted(drifted.size(), summarize(drifted));
            explanation = reasoningServiceClient
                    .reason(repositoryId, "drift_explanation", query, null, List.of(), List.of())
                    .answer();
        }

        DriftReport report = DriftReport.builder()
                .repositoryId(repositoryId)
                .driftedFilesJson(toJson(drifted))
                .explanation(explanation)
                .build();
        report = driftReportRepository.save(report);

        return new DriftReportResponse(report.getId(), repositoryId, drifted, explanation, report.getCreatedAt());
    }
    private String summarize(List<DriftedFile> drifted) {
        return drifted.stream().limit(5)
                .map(d -> "%s (fan-in %d\u2192%d, fan-out %d\u2192%d)"
                        .formatted(d.path(), d.prevFanIn(), d.newFanIn(), d.prevFanOut(), d.newFanOut()))
                .collect(Collectors.joining("; "));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize analysis result", e);
        }
    }
}