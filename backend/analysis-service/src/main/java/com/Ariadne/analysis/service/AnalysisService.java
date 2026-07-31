package com.Ariadne.analysis.service;

import com.Ariadne.analysis.client.GraphServiceClient;
import com.Ariadne.analysis.dto.AnalysisReportResponse;
import com.Ariadne.analysis.dto.RepositoryGraphResponse;
import com.Ariadne.analysis.entity.AnalysisReport;
import com.Ariadne.analysis.repository.AnalysisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final GraphServiceClient graphServiceClient;
    private final AnalysisReportRepository reportRepository;

    @Transactional
    public AnalysisReportResponse runBasicAnalysis(UUID repositoryId) {
        RepositoryGraphResponse graph = graphServiceClient.getRepositoryGraph(repositoryId.toString()).data();

        AnalysisReport report = AnalysisReport.builder()
                .repositoryId(repositoryId)
                .fileCount(graph.files().size())
                .build();
        report = reportRepository.save(report);

        return toResponse(report);
    }

    private AnalysisReportResponse toResponse(AnalysisReport r) {
        return new AnalysisReportResponse(r.getId(), r.getRepositoryId(), r.getFileCount(), r.getCreatedAt());
    }
}