package com.Ariadne.analysis.service;

import com.Ariadne.analysis.client.GraphServiceClient;
import com.Ariadne.analysis.dto.AnalysisReportResponse;
import com.Ariadne.analysis.dto.RepositoryGraphResponse;
import com.Ariadne.analysis.entity.AnalysisReport;
import com.Ariadne.analysis.repository.AnalysisReportRepository;
import com.Ariadne.shared.events.AnalysisCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final String TOPIC = "analysis.completed";

    private final GraphServiceClient graphServiceClient;
    private final AnalysisReportRepository reportRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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
}