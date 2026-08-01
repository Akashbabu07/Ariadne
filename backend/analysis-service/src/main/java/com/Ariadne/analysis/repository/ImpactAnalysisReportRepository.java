package com.Ariadne.analysis.repository;

import com.Ariadne.analysis.entity.ImpactAnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ImpactAnalysisReportRepository extends JpaRepository<ImpactAnalysisReport, UUID> {
    List<ImpactAnalysisReport> findByRepositoryId(UUID repositoryId);
}