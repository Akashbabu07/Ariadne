package com.Ariadne.project.dto;

import java.time.Instant;
import java.util.UUID;

public record RepositoryResponse(
        UUID id, UUID projectId, String gitUrl, String defaultBranch,
        String syncStatus, Instant lastSyncedAt, Instant createdAt
) {}
