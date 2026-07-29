package com.Ariadne.scheduler.dto;

public record RepositoryResponse(
        String id, String projectId, String gitUrl, String defaultBranch,
        String syncStatus, String lastSyncedAt, String createdAt
) {}