package com.Ariadne.analysis.dto;

import java.time.Instant;
import java.util.UUID;

public record AnalysisReportResponse(UUID id, UUID repositoryId, int fileCount, Instant createdAt) {}