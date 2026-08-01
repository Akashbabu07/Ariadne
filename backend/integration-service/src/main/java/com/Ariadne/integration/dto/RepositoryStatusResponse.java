package com.Ariadne.integration.dto;

import java.time.Instant;
import java.util.UUID;

public record RepositoryStatusResponse(
        UUID id, UUID projectId, String gitUrl, String defaultBranch,
        String syncStatus, Instant lastSyncedAt, Instant createdAt
) {}