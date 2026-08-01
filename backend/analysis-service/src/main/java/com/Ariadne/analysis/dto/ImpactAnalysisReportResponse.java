package com.Ariadne.analysis.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ImpactAnalysisReportResponse(
        UUID id, UUID repositoryId, String targetPath,
        List<String> impactedFiles, String explanation, Instant createdAt
) {}