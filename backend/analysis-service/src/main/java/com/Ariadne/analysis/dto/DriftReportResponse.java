package com.Ariadne.analysis.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DriftReportResponse(
        UUID id, UUID repositoryId, List<DriftedFile> driftedFiles, String explanation, Instant createdAt
) {}