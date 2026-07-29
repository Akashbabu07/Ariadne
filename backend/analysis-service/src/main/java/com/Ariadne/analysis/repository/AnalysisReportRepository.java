package com.Ariadne.analysis.repository;

import com.Ariadne.analysis.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {
    List<AnalysisReport> findByRepositoryId(UUID repositoryId);
}