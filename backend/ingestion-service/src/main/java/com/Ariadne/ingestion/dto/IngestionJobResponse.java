package com.Ariadne.ingestion.dto;

import java.time.Instant;
import java.util.UUID;

public record IngestionJobResponse(
        UUID id, UUID repositoryId, String gitUrl, String status,
        String errorMessage, Instant createdAt, Instant updatedAt
) {}