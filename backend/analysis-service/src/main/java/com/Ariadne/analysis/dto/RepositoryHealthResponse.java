package com.Ariadne.analysis.dto;

import java.time.Instant;
import java.util.UUID;

public record RepositoryHealthResponse(
        UUID repositoryId,
        String syncStatus,
        Instant lastSyncedAt,
        int fileCount,
        long dependencyEdgeCount,
        Instant lastAnalysisAt,
        boolean driftDetected,
        int driftedFileCount,
        Instant lastDriftCheckAt
) {}